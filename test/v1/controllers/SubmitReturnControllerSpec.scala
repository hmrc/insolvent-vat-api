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
import utils._
import v1.mocks.requestParsers.MockSubmitReturnRequestParser
import v1.mocks.{MockCurrentDateTime, MockIdGenerator}
import v1.mocks.services.{MockSubmitReturnRequestService, _}
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.{SubmitReturnRawData, SubmitReturnRequest, SubmitReturnRequestBody}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubmitReturnControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockAppConfig
    with MockSubmitReturnRequestParser
    with MockSubmitReturnRequestService
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

    val controller: SubmitReturnController = new SubmitReturnController(
      mockEnrolmentsAuthService,
      requestParser = mockSubmitReturnRequestParser,
      service = mockSubmitReturnRequestService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockEnrolmentsAuthService.authoriseUser()
    MockCurrentDateTime.getCurrentDate.returns(date).anyNumberOfTimes()
    MockIdGenerator.getUid.returns(uid).once()
    MockIdGenerator.getUid.returns(correlationId).anyNumberOfTimes()
    MockedAppConfig.apiGatewayContext.returns("baseUrl").anyNumberOfTimes()
  }

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
    vatDueAcquisitions = 3000.00,
    totalVatDue = 10000.00,
    vatReclaimedCurrPeriod = 1000.00,
    netVatDue = 9000.00,
    totalValueSalesExVAT = 1000.00,
    totalValuePurchasesExVAT = 200.00,
    totalValueGoodsSuppliedExVAT = 100.00,
    totalAcquisitionsExVAT = 540.00,
    receivedAt = "2020-05-05T12:01:00Z",
    uniqueId = "0123456789"
  )

  val submitReturnRequest: SubmitReturnRequest =
    SubmitReturnRequest(
      vrn = Vrn(vrn),
      body = requestBody
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

  "submitReturn" when {
    "return OK" should {
      "happy path" in new Test {

        MockSubmitReturnRequestParser
          .parse(submitRequestRawData)
          .returns(Right(submitReturnRequest))

        MockSubmitReturnService
          .submitReturn(submitReturnRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        private val result: Future[Result] = controller.submitReturn(vrn)(fakePostRequest(submitRequestBodyJson))

        status(result) shouldBe CREATED
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockSubmitReturnRequestParser
              .parse(submitRequestRawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.submitReturn(vrn)(fakePostRequest(submitRequestBodyJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
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

            MockSubmitReturnRequestParser
              .parse(submitRequestRawData)
              .returns(Right(submitReturnRequest))

            MockSubmitReturnService
              .submitReturn(submitReturnRequest)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.submitReturn(vrn)(fakePostRequest(submitRequestBodyJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (VrnFormatError, BAD_REQUEST),
          (PeriodKeyFormatError, BAD_REQUEST),
          (RuleIncorrectOrEmptyBodyError, BAD_REQUEST),
          (ValueFormatError, BAD_REQUEST),
          (UniqueIDFormatError, BAD_REQUEST),
          (ReceivedAtFormatError, BAD_REQUEST),
          (BadRequestError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}
