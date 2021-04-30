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

package v1.controllers

import mocks.MockAppConfig
import org.joda.time.DateTime
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsJson, Result}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import utils.{DateUtils, MockMetrics}
import v1.mocks.requestParsers.MockAmendSampleRequestParser
import v1.mocks.services.{MockAmendSampleService, MockAuditService, MockEnrolmentsAuthService}
import v1.models.audit.{AuditDetail, AuditError, AuditEvent, AuditResponse, SampleAuditResponse}
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.{SubmitReturnRawData, SubmitReturnRequest, SubmitReturnRequestBody}
import v1.models.response.SubmitReturnResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubmitReturnControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockAppConfig
    with MockAmendSampleService
    with MockAmendSampleRequestParser
    with MockCurrentDateTime
    with MockIdGenerator
    with MockAuditService {

  val date: DateTime = DateTime.parse("2017-01-01T00:00:00.000Z")
  val fmt: String = DateUtils.dateTimePattern
  val vrn: String = "123456789"
  val correlationId: String = "X-ID"
  val uid: String = "a5894863-9cd7-4d0d-9eee-301ae79cbae6"
  val periodKey: String = "A1A2"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val mockedMetrics: Metrics = new MockMetrics

    val controller: SubmitReturnController = new SubmitReturnController(
      authService = mockEnrolmentsAuthService,
      requestParser = mockAmendSampleRequestParser,
      mockAmendSampleService,
      auditService = mockAuditService,
      cc,
      idGenerator = mockIdGenerator
      dateTime = mockCurrentDateTime,
      mockedMetrics
    )

    MockEnrolmentsAuthService.authoriseUser()
    MockCurrentDateTime.getCurrentDate.returns(date).anyNumberOfTimes()
    MockIdGenerator.getUid.returns(uid).once()
    MockIdGenerator.getUid.returns(correlationId).anyNumberOfTimes()
  }

  def event(auditResponse: AuditResponse): AuditEvent[AuditDetail] =
    AuditEvent(
      auditType = "AmendACustomEmployment",
      transactionName = "amend-a-custom-employment",
      detail = AuditDetail(
        userType = "Individual",
        agentReferenceNumber = None,
        params = Map("vrn" -> vrn),
        request = Some(requestBodyJson),
        `X-CorrelationId` = correlationId,
        response = auditResponse
      )
    )

  private val requestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "periodKey": "#001",
      |   "vatDueSales": 	7000.00,
      |   "vatDueAcquisitions": 	3000.00,
      |   "vatDueTotal": 	10000,
      |   "vatReclaimedCurrPeriod": 	1000,
      |   "vatDueNet": 	9000,
      |   "totalValueSalesExVAT": 	1000,
      |   "totalValuePurchasesExVAT": 	200,
      |   "totalValueGoodsSuppliedExVAT": 	100,
      |   "totalAllAcquisitionsExVAT": 	540,
      |   "receivedAt":  "2020-05-05T12:01:00Z",
      |   "uniqueId": "0123456789"
      |}
    """.stripMargin
  )

  val submitRequestBody: SubmitReturnRequestBody = SubmitReturnRequestBody(
    periodKey = "#001",
    vatDueSales = 7000.00,
    vatDueAcquisitions = 3000,
    totalVatDue = 10000,
    vatReclaimedCurrPeriod = 1000,
    netVatDue = 9000,
    totalValueSalesExVAT = 1000,
    totalValuePurchasesExVAT = 200,
    totalValueGoodsSuppliedExVAT = 100,
    totalAcquisitionsExVAT = 540,
    receivedAt = "2020-05-05T12:01:00Z",
    uniqueId = "0123456789"
  )

  val submitRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "periodKey": "#001",
      |   "vatDueSales": 	7000.00,
      |   "vatDueAcquisitions": 	3000.00,
      |   "vatDueTotal": 	10000,
      |   "vatReclaimedCurrPeriod": 	1000,
      |   "vatDueNet": 	9000,
      |   "totalValueSalesExVAT": 	1000,
      |   "totalValuePurchasesExVAT": 	200,
      |   "totalValueGoodsSuppliedExVAT": 	100,
      |   "totalAllAcquisitionsExVAT": 	540,
      |   "receivedAt":  "2020-05-05T12:01:00Z",
      |   "uniqueId": "0123456789"
      |}
      |""".stripMargin
  )

  val submitRequestRawData: SubmitReturnRawData = SubmitReturnRawData(
    vrn = vrn,
    body = AnyContentAsJson(submitRequestBodyJson)
  )

  val requestBody: SubmitReturnRequestBody = SubmitReturnRequestBody(
    periodKey = "#001",
    vatDueSales = 7000.00,
    vatDueAcquisitions = 3000,
    totalVatDue = 10000,
    vatReclaimedCurrPeriod = 1000,
    netVatDue = 9000,
    totalValueSalesExVAT = 1000,
    totalValuePurchasesExVAT = 200,
    totalValueGoodsSuppliedExVAT = 100,
    totalAcquisitionsExVAT = 540,
    receivedAt = "2020-05-05T12:01:00Z",
    uniqueId = "0123456789"
  )

  val submitReturnRequest: SubmitReturnRequest =
    SubmitReturnRequest(
      vrn = Vrn(vrn),
      SubmitReturnRequestBody
    )

  val submitReturnResponse: SubmitReturnResponse =
    SubmitReturnResponse(
      processingDate = DateTime.parse("2017-01-01T00:00:00.000Z"),
      paymentIndicator = Some("DD"),
      formBundleNumber = Some("123456789012"),
      chargeRefNumber = Some("SKDJGFH9URGT")
    )

  val submitReturnResponseJson: JsValue = Json.parse(
    """
      |{
      |   "processingDate": "2017-01-01T00:00:00.000Z",
      |   "formBundleNumber": "123456789012",
      |   "paymentIndicator": "DD",
      |   "chargeRefNumber": "SKDJGFH9URGT"
      |}
      |""".stripMargin
  )

  val hateoasResponse: JsValue = Json.parse(
    s"""
       |{
       |   "links":[
       |      {
       |         "href":"/insolvent/vat-api/$vrn",
       |         "rel":"submit-return",
       |         "method":"POST"
       |      },
       |   ]
       |}
    """.stripMargin
  )

  "submitReturn" when {
    "a valid request is supplied" should {
      "return the expected data on a successful service call" in new Test {

        MockAmendSampleRequestParser
          .parse(submitRequestRawData)
          .returns(Right(submitReturnRequest))

        MockAmendSampleService
          .amendSample(submitReturnRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, submitReturnResponse))))

        private val result: Future[Result] = controller.submitReturn(vrn)(fakePostRequest(submitRequestBodyJson))

        status(result) shouldBe CREATED
        contentAsJson(result) shouldBe submitReturnResponseJson
        header("X-CorrelationId", result) shouldBe Some(correlationId)
        header("Receipt-Timestamp", result).getOrElse("No Header") should fullyMatch.regex(DateUtils.isoInstantDateRegex)

        val auditResponse: AuditResponse = AuditResponse(CREATED, None, Some(submitReturnResponseJson))
        MockedAuditService.verifyAuditEvent(event(auditResponse)).once
      }
    }

    "submitted request is not obeying the RULE validations" should {
      "return the errors" in new Test {

        val submitRequestBodyJsonWithInvalidFinalisedFormat: JsValue = Json.parse(
          """
            |{
            |   "periodKey": "#001",
            |   "vatDueSales": 	7000.00,
            |   "vatDueAcquisitions": 	3000.00,
            |   "totalVatDue": 	4000,
            |   "vatReclaimedCurrPeriod": 	1000,
            |   "netVatDue": 	9000,
            |   "totalValueSalesExVAT": 	1000,
            |   "totalValuePurchasesExVAT": 	200,
            |   "totalValueGoodsSuppliedExVAT": 	100,
            |   "totalAllAcquisitionsExVAT": 	540,
            |   "receivedAt":  "2020-05-05T12:01:00Z",
            |   "uniqueId": "0123456789"
            |}
            |""".stripMargin
        )

        val expectedError: JsValue = Json.parse(
          s"""
             |{
             |        "code" : "INVALID_REQUEST",
             |        "message" : "Invalid request",
             |        "errors" : [ {
             |          "code" : "VAT_TOTAL_VALUE",
             |          "message" : "totalVatDue should be equal to vatDueSales + vatDueAcquisitions",
             |          "path" : "/totalVatDue"
             |        }, {
             |          "code" : "VAT_NET_VALUE",
             |          "message" : "netVatDue should be the difference between the largest and the smallest values among totalVatDue and vatReclaimedCurrPeriod",
             |          "path" : "/netVatDue"
             |        } ]
             |      }
      """.stripMargin)

        MockAmendSampleRequestParser
          .parse(submitRequestRawData)
          .returns(Left(ErrorWrapper(correlationId, BadRequestError)))

        private val result: Future[Result] = controller.submitReturn(vrn)(fakePostRequest(submitRequestBodyJsonWithInvalidFinalisedFormat))

        status(result) shouldBe BAD_REQUEST
        contentAsJson(result) shouldBe expectedError
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse (OK, None, Some(hateoasResponse))
        MockedAuditService.verifyAuditEvent(event(auditResponse)).once
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockAmendSampleRequestParser
              .parse(submitRequestRawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.submitReturn(vrn)(fakePostRequest(submitRequestBodyJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(error.code))), None)
            MockedAuditService.verifyAuditEvent(event(auditResponse)).once
          }
        }

        val input = Seq(
          (VrnFormatError, BAD_REQUEST),
          (PeriodKeyFormatError, BAD_REQUEST),
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a ${mtdError.code} error is returned from the service" in new Test {

            MockAmendSampleRequestParser
              .parse(submitRequestRawData)
              .returns(Right(submitReturnRequest))

            MockAmendSampleService
              .amendSample(submitReturnRequest)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.submitReturn(vrn)(fakePostRequest(submitRequestBodyJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)
            MockedAuditService.verifyAuditEvent(event(auditResponse)).once
          }
        }

        val input = Seq(
          (RuleIncorrectOrEmptyBodyError, BAD_REQUEST),
          (VrnFormatErrorDes, BAD_REQUEST),
          (BadRequestError, BAD_REQUEST),
          (VrnFormatError, BAD_REQUEST),
          (PeriodKeyFormatErrorDes, BAD_REQUEST),
          (PeriodKeyFormatError, BAD_REQUEST),
          (FormatValueError, BAD_REQUEST),
          (FormatUniqueIDError, BAD_REQUEST),
          (PeriodKeyFormatErrorDes, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}
