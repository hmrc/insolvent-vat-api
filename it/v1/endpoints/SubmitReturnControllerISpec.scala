/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec
import v1.models.errors._
import v1.stubs.{AuditStub, DesStub}

class SubmitReturnControllerISpec extends IntegrationBaseSpec {

  private trait Test {
    val vrn: String = "123456789"

    val desResponse: JsValue = Json.parse(
      """
        |{
        |  "processingDate": "2017-10-18T00:01:00Z",
        |  "formBundleNumber": "123456789012",
        |  "paymentIndicator": "DD",
        |  "chargeRefNumber": "SKDJGFH9URGT"
        |}
      """.stripMargin
    )

    val requestJson: JsValue = Json.parse(
      """
        |{
        |  "periodKey": "18A1",
        |  "vatDueSales": 105.50,
        |  "vatDueAcquisitions": -100.45,
        |  "totalVatDue": 5.05,
        |  "vatReclaimedCurrPeriod": 105.15,
        |  "netVatDue": 100.10,
        |  "totalValueSalesExVAT": 300,
        |  "totalValuePurchasesExVAT": 300,
        |  "totalValueGoodsSuppliedExVAT": 3000,
        |  "totalAcquisitionsExVAT": 3000,
        |  "receivedAt": "2020-05-05T12:01:00Z",
        |  "uniqueId": "0123456789"
        |}
      """.stripMargin
    )

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
       |{
       |   "code": "$code",
       |   "reason": "des message"
       |}
    """.stripMargin

  }

  "Calling the Insolvent Submit VAT Return endpoint" when {
    "return a 201 status code with expected body" should {
      "a valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          DesStub.onSuccess(DesStub.POST, desUrl, OK, desResponse)
        }

        val response: WSResponse = await(request().post(requestJson))
        response.status shouldBe CREATED
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {

      val validRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |  "periodKey": "18A1",
          |  "vatDueSales": 105.50,
          |  "vatDueAcquisitions": -100.45,
          |  "totalVatDue": 5.05,
          |  "vatReclaimedCurrPeriod": 105.15,
          |  "netVatDue": 100.10,
          |  "totalValueSalesExVAT": 300,
          |  "totalValuePurchasesExVAT": 300,
          |  "totalValueGoodsSuppliedExVAT": 3000,
          |  "totalAcquisitionsExVAT": 3000,
          |  "receivedAt": "2020-05-05T12:01:00Z",
          |  "uniqueId": "0123456789"
          |}
        """.stripMargin
      )

      val invalidPeriodKeyRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |  "periodKey": "period",
          |  "vatDueSales": 105.50,
          |  "vatDueAcquisitions": -100.45,
          |  "totalVatDue": 5.05,
          |  "vatReclaimedCurrPeriod": 105.15,
          |  "netVatDue": 100.10,
          |  "totalValueSalesExVAT": 300,
          |  "totalValuePurchasesExVAT": 300,
          |  "totalValueGoodsSuppliedExVAT": 3000,
          |  "totalAcquisitionsExVAT": 3000,
          |  "receivedAt": "2020-05-05T12:01:00Z",
          |  "uniqueId": "0123456789"
          |}
        """.stripMargin
      )

      val invalidReceivedAtRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |  "periodKey": "18A1",
          |  "vatDueSales": 105.50,
          |  "vatDueAcquisitions": -100.45,
          |  "totalVatDue": 5.05,
          |  "vatReclaimedCurrPeriod": 105.15,
          |  "netVatDue": 100.10,
          |  "totalValueSalesExVAT": 300,
          |  "totalValuePurchasesExVAT": 300,
          |  "totalValueGoodsSuppliedExVAT": 3000,
          |  "totalAcquisitionsExVAT": 3000,
          |  "receivedAt": "datetime",
          |  "uniqueId": "0123456789"
          |}
        """.stripMargin
      )

      val invalidUniqueIdRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |  "periodKey": "18A1",
          |  "vatDueSales": 105.50,
          |  "vatDueAcquisitions": -100.45,
          |  "totalVatDue": 5.05,
          |  "vatReclaimedCurrPeriod": 105.15,
          |  "netVatDue": 100.10,
          |  "totalValueSalesExVAT": 300,
          |  "totalValuePurchasesExVAT": 300,
          |  "totalValueGoodsSuppliedExVAT": 3000,
          |  "totalAcquisitionsExVAT": 3000,
          |  "receivedAt": "2020-05-05T12:01:00Z",
          |  "uniqueId": "0123456789123"
          |}
        """.stripMargin
      )

      val invalidVatDueSalesRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |  "periodKey": "18A1",
          |  "vatDueSales": 105.505,
          |  "vatDueAcquisitions": -100.45,
          |  "totalVatDue": 5.05,
          |  "vatReclaimedCurrPeriod": 105.15,
          |  "netVatDue": 100.10,
          |  "totalValueSalesExVAT": 300,
          |  "totalValuePurchasesExVAT": 300,
          |  "totalValueGoodsSuppliedExVAT": 3000,
          |  "totalAcquisitionsExVAT": 3000,
          |  "receivedAt": "2020-05-05T12:01:00Z",
          |  "uniqueId": "0123456789"
          |}
        """.stripMargin
      )

      val allInvalidValueRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |  "periodKey": "period",
          |  "vatDueSales": 105.505,
          |  "vatDueAcquisitions": -100.453,
          |  "totalVatDue": 5.052,
          |  "vatReclaimedCurrPeriod": 105.154,
          |  "netVatDue": 100.102,
          |  "totalValueSalesExVAT": 300.99,
          |  "totalValuePurchasesExVAT": 300.99,
          |  "totalValueGoodsSuppliedExVAT": 3000.99,
          |  "totalAcquisitionsExVAT": 3000.99,
          |  "receivedAt": "time",
          |  "uniqueId": "0123456789123"
          |}
        """.stripMargin
      )

      val nonValidRequestBodyJson: JsValue = Json.parse(
        """
          |{
          |  "periodKey": true,
          |  "vatDueSales": 105.50,
          |  "vatDueAcquisitions": -100.45,
          |  "totalVatDue": 5.05,
          |  "vatReclaimedCurrPeriod": 105.15,
          |  "netVatDue": 100.10,
          |  "totalValueSalesExVAT": 300,
          |  "totalValuePurchasesExVAT": 300,
          |  "totalValueGoodsSuppliedExVAT": 3000,
          |  "totalAcquisitionsExVAT": 3000
          |}
        """.stripMargin
      )

      val emptyRequestBodyJson: JsValue = JsObject.empty

      val valueFormatError: MtdError = ValueFormatError.copy(
        message = "The field should be between -9999999999999.99 and 9999999999999.99",
        paths = Some(Seq("/vatDueSales"))
      )

      val allInvalidValueErrors: Seq[MtdError] = Seq(
        ReceivedAtFormatError,
        ValueFormatError.copy(
          paths = Some(List(
            "/vatDueSales",
            "/vatDueAcquisitions",
            "/totalVatDue",
            "/vatReclaimedCurrPeriod"
          )),
          message = "The field should be between -9999999999999.99 and 9999999999999.99"
        ),
        UniqueIDFormatError,
        ValueFormatError.copy(
          paths = Some(List(
            "/totalValueSalesExVAT",
            "/totalValuePurchasesExVAT",
            "/totalValueGoodsSuppliedExVAT",
            "/totalAcquisitionsExVAT"
          )),
          message = "The field should be between -9999999999999 and 9999999999999"
        ),
        ValueFormatError.copy(
          paths = Some(List(
            "/netVatDue"
          )),
          message = "The field should be between 0.00 and 99999999999.99"
        ),
        PeriodKeyFormatError
      )

      val nonValidRequestBodyErrors: MtdError = RuleIncorrectOrEmptyBodyError.copy(
        paths = Some(Seq("/receivedAt", "/uniqueId", "/periodKey").sorted)
      )

      def validationErrorTest(requestVrn: String, requestBody: JsValue, expectedStatus: Int,
                              expectedBody: ErrorWrapper, scenario: Option[String]): Unit = {
        s"validation fails with ${expectedBody.error} error ${scenario.getOrElse("")}" in new Test {

          override val vrn: String = requestVrn
          override val requestJson: JsValue = requestBody

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            StubMapping.NOT_CONFIGURED
          }

          val response: WSResponse = await(request().post(requestJson))
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      val input = Seq(
        ("badVrn", validRequestBodyJson, BAD_REQUEST, ErrorWrapper("X-123", VrnFormatError, None),None),
        ("123456789", invalidPeriodKeyRequestBodyJson, BAD_REQUEST, ErrorWrapper("X-123", PeriodKeyFormatError, None),None),
        ("123456789", invalidReceivedAtRequestBodyJson, BAD_REQUEST, ErrorWrapper("X-123", ReceivedAtFormatError, None),None),
        ("123456789", invalidUniqueIdRequestBodyJson, BAD_REQUEST, ErrorWrapper("X-123", UniqueIDFormatError, None),None),
        ("123456789", emptyRequestBodyJson, BAD_REQUEST, ErrorWrapper("X-123", RuleIncorrectOrEmptyBodyError),None),
        ("123456789", invalidVatDueSalesRequestBodyJson, BAD_REQUEST, ErrorWrapper("X-123", valueFormatError),None),
        ("123456789", allInvalidValueRequestBodyJson, BAD_REQUEST, ErrorWrapper("X-123", BadRequestError, Some(allInvalidValueErrors)),None),
        ("123456789", nonValidRequestBodyJson, BAD_REQUEST, ErrorWrapper("X-123", nonValidRequestBodyErrors), Some("invalid request body format and missing mandatory fields"))
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
