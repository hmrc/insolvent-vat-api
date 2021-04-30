/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSRequest
import support.IntegrationBaseSpec
import v1.models.errors._
import v1.stubs.{AuditStub, AuthStub, DesStub}

class SubmitReturnControllerISpec extends IntegrationBaseSpec {

  private trait Test {
    val vrn = "123456789"

    val desResponseJson: JsValue = Json.parse(
      """
        |{
        |    "processingDate": "2018-03-01T11:43:43.195Z",
        |    "paymentIndicator": "BANK",
        |    "formBundleNumber": "891713832155"
        |    "chargeRefNumber" = "aCxFaNx0FZsCvyWF"
        |}
    """.stripMargin
    )

    val mtdResponseJson: JsValue = Json.parse(
      """
        |{
        |	"processingDate": "2018-03-01T11:43:43.195Z",
        |	"paymentIndicator": "BANK",
        |	"formBundleNumber": "891713832155"
        | "chargeRefNumber" = "aCxFaNx0FZsCvyWF"
        |}
    """.stripMargin
    )

    val requestJson: JsValue = Json.parse(
      s"""
         |{
         |        "periodKey" : "18A1",
         |        "vatDueSales" : 1000,
         |        "vatDueAcquisitions" : -1000,
         |        "totalVatDue" : 0,
         |        "vatReclaimedCurrPeriod" : 100,
         |        "netVatDue" : 100,
         |        "totalValueSalesExVAT" : 5000,
         |        "totalValuePurchasesExVAT" : 1000,
         |        "totalValueGoodsSuppliedExVAT" : 9999999999999,
         |        "totalAcquisitionsExVAT" : 9999999999999,
         |        "receivedAt":  "2020-05-05T12:01:00Z",
         |        "uniqueId": "0123456789"
         |}
    """.stripMargin)

    def uri: String = s"/$vrn/returns"
    def desUrl: String = s"/enterprise/return/vat/$vrn"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()

      buildRequest(s"/sample/$vrn")
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }

    def errorBody(code: String): String =
    s"""
       |      {
       |        "code": "$code",
       |        "reason": "des message"
       |      }
    """.stripMargin

  }

  "Submit VAT Return endpoint" when {
    "return a 201 status code with expected body" should {
      "a valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          DesStub.onSuccess(DesStub.POST, desUrl, OK, desResponseJson)
        }

        private val response = await(request.post(requestJson))
        response.status shouldBe CREATED
        response.json shouldBe mtdResponseJson
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return a 400 status code with VRN_INVALID" should {
      "a request is made with invalid vrn and body" in new Test {

        override val vrn = "123456789a"
        val submitRequestBodyJsonWithInvalidFinalisedFormat: String =
          """
            |{
            |   "periodKey": 1,
            |   "vatDueSales": 	Invalid Json,
            |   "vatDueAcquisitions": 	3000.00,
            |   "totalVatDue": 	10000,
            |   "vatReclaimedCurrPeriod": 	1000,
            |   "netVatDue": 	9000,
            |   "totalValueSalesExVAT": 	1000,
            |   "totalValuePurchasesExVAT": 	200,
            |   "totalValueGoodsSuppliedExVAT": 	100000,
            |   "totalAcquisitionsExVAT": 	540,
            |   "receivedAt":  "2020-05-05T12:01:00Z",
            |   "uniqueId": "0123456789"
            |}
            |""".stripMargin

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
        }

        private val response = await(request.post(submitRequestBodyJsonWithInvalidFinalisedFormat))
        response.status shouldBe BAD_REQUEST
        response.json shouldBe Json.toJson(VrnFormatError)
      }
    }

    "des service error" when {
      def serviceErrorTest(desStatus: Int, desCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"des returns an $desCode error and status $desStatus" in new Test {

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            DesStub.onError(DesStub.POST, desUrl, desStatus, errorBody(desCode))
          }

          private val response = await(request.post(requestJson))
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      val input = Seq(
        (BAD_REQUEST, "INVALID_VRN", BAD_REQUEST, VrnFormatErrorDes),
        (BAD_REQUEST, "INVALID_PERIODKEY", BAD_REQUEST, PeriodKeyFormatErrorDes),
        (BAD_REQUEST, "INVALID_PAYLOAD", BAD_REQUEST, BadRequestError),
        (FORBIDDEN, "TAX_PERIOD_NOT_ENDED", FORBIDDEN, TaxPeriodNotEnded),
        (CONFLICT, "DUPLICATE_SUBMISSION", FORBIDDEN, DuplicateVatSubmission),
        (FORBIDDEN, "NOT_FOUND_VRN", INTERNAL_SERVER_ERROR, DownstreamError),
        (FORBIDDEN, "INSOLVENT_TRADER", FORBIDDEN, RuleInsolventTraderError),
        (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_ORIGINATOR_ID", INTERNAL_SERVER_ERROR, DownstreamError),
        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_SUBMISSION", INTERNAL_SERVER_ERROR, DownstreamError)
      )

      input.foreach(args => (serviceErrorTest _).tupled(args))

    }
  }
}
