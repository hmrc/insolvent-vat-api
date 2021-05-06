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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsJson, Result}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.requestParsers.MockSubmitReturnRequestParser
import v1.mocks.MockIdGenerator
import v1.mocks.services.{MockSubmitReturnService, _}
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.{SubmitReturnRawData, SubmitReturnRequest, SubmitReturnRequestBody}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubmitReturnControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockSubmitReturnRequestParser
    with MockSubmitReturnService
    with MockIdGenerator {

  val vrn: String = "123456789"
  val correlationId: String = "X-ID"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller: SubmitReturnController = new SubmitReturnController(
      mockEnrolmentsAuthService,
      requestParser = mockSubmitReturnRequestParser,
      service = mockSubmitReturnService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.getUid.returns(correlationId).anyNumberOfTimes()
  }

  val submitRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "periodKey": "#001",
      |   "vatDueSales": 7000.00,
      |   "vatDueAcquisitions": 3000.00,
      |   "vatDueTotal": 10000,
      |   "vatReclaimedCurrPeriod": 1000,
      |   "vatDueNet": 9000,
      |   "totalValueSalesExVAT": 1000,
      |   "totalValuePurchasesExVAT": 200,
      |   "totalValueGoodsSuppliedExVAT": 100,
      |   "totalAllAcquisitionsExVAT": 540,
      |   "receivedAt": "2020-05-05T12:01:00Z",
      |   "uniqueId": "0123456789"
      |}
    """.stripMargin
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

  val submitReturnRequest: SubmitReturnRequest = SubmitReturnRequest(
    vrn = Vrn(vrn),
    body = requestBody
  )

  "submitReturn" when {
    "return CREATED" should {
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
          (ValueFormatError, BAD_REQUEST),
          (RuleIncorrectOrEmptyBodyError, BAD_REQUEST),
          (UniqueIDFormatError, BAD_REQUEST),
          (ReceivedAtFormatError, BAD_REQUEST),
          (BadRequestError, BAD_REQUEST),
          (PeriodKeyFormatError, BAD_REQUEST)
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
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}
