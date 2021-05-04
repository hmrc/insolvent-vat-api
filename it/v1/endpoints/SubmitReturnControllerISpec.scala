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
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec
import v1.models.errors._
import v1.stubs.{AuditStub, AuthStub, DesStub}

class SubmitReturnControllerISpec extends IntegrationBaseSpec {

  private trait Test {
    val vrn = "123456789"
    val periodKey: String = "#001"

    val desResponse: JsValue = Json.parse(
      s"""
         |{
         |  "processingDate": "2017-10-18T00:01:00Z",
         |  "formBundleNumber": "123456789012",
         |  "paymentIndicator": "DD",
         |  "chargeRefNumber": "SKDJGFH9URGT"
         |}
         |""".stripMargin
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
      buildRequest(uri)
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

  val mtdResponse: JsValue = Json.parse(
    s"""
       |{
       |   "links":[
       |      {
       |         "href":"/insolvent-vat-api/vrn",
       |         "method":"POST",
       |         "rel":"submit-return"
       |      }
       |   ]
       |}
    """.stripMargin
  )

  "Submit Insolvent-Vat submitReturn endpoint" when {
    "return a 200 status code with expected body" should {
      "a valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          DesStub.onSuccess(DesStub.POST, desUrl, OK, desResponse)
        }

        val response: WSResponse = await(request().post(requestJson))
        response.status shouldBe OK
        response.json shouldBe mtdResponse
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {

      def validationErrorTest(requestVrn: String,
                              requestPeriodKey: String,
                              expectedStatus: Int,
                              expectedBody: MtdError): Unit = {
        s"validation fails with ${expectedBody.code} error" in new Test {

          override val vrn: String = requestVrn
          override val periodKey: String = requestPeriodKey

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
          }

          val response: WSResponse = await(request().post(requestJson))
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      val input = Seq(
        ("badVrn", "AA11", BAD_REQUEST, VrnFormatError)
      )

      input.foreach(args => (validationErrorTest _).tupled(args))
    }

    "des service error" when {
      def serviceErrorTest(desStatus: Int, desCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"des returns an $desCode error and status $desStatus" in new Test {

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            DesStub.onError(DesStub.POST, desUrl, desStatus, errorBody(desCode))
          }

          val response: WSResponse = await(request().post(requestJson))
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
        }
      }
      val input = Seq(
        (BAD_REQUEST, "INVALID_VRN", BAD_REQUEST, VrnFormatError),
        (BAD_REQUEST, "INVALID_PERIODKEY", BAD_REQUEST, PeriodKeyFormatError),
        (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, DownstreamError),
        (FORBIDDEN, "TAX_PERIOD_NOT_ENDED", INTERNAL_SERVER_ERROR, DownstreamError),
        (CONFLICT, "DUPLICATE_SUBMISSION", INTERNAL_SERVER_ERROR, DownstreamError),
        (FORBIDDEN, "NOT_FOUND_VRN", INTERNAL_SERVER_ERROR, DownstreamError),
        (FORBIDDEN, "INSOLVENT_TRADER", INTERNAL_SERVER_ERROR, DownstreamError),
        (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_ORIGINATOR_ID", INTERNAL_SERVER_ERROR, DownstreamError),
        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_SUBMISSION", INTERNAL_SERVER_ERROR, DownstreamError)
      )

      input.foreach(args => (serviceErrorTest _).tupled(args))

    }
  }
}
