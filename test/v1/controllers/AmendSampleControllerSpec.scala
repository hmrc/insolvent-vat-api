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
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.MockIdGenerator
import v1.mocks.requestParsers.MockAmendSampleRequestParser
import v1.mocks.services.{MockAmendSampleService, MockAuditService, MockEnrolmentsAuthService}
import v1.models.audit.{AuditError, AuditEvent, SampleAuditDetail, SampleAuditResponse}
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.amendSample.{AmendSampleRawData, AmendSampleRequest, AmendSampleRequestBody}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AmendSampleControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockAppConfig
    with MockAmendSampleService
    with MockAmendSampleRequestParser
    with MockAuditService
    with MockIdGenerator {

  val vrn: String = "123456789"
  val correlationId: String = "X-123"

  val requestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "data" : "someData"
      |}
    """.stripMargin
  )

  val rawData: AmendSampleRawData = AmendSampleRawData(
    vrn = vrn,
    body = requestBodyJson
  )

  val requestBody: AmendSampleRequestBody = AmendSampleRequestBody(
    data = "someData"
  )

  val requestData: AmendSampleRequest = AmendSampleRequest(
    vrn = Vrn(vrn),
    body = requestBody
  )

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new AmendSampleController(
      authService = mockEnrolmentsAuthService,
      appConfig = mockAppConfig,
      requestParser = mockAmendSampleRequestParser,
      service = mockAmendSampleService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockEnrolmentsAuthService.authoriseUser()
    MockedAppConfig.apiGatewayContext.returns("organisations/insolvent/vat").anyNumberOfTimes()
    MockIdGenerator.generateCorrelationId.returns(correlationId)
  }

  val responseBody: JsValue = Json.parse(
    """
      |{
      |  "links": [
      |    {
      |      "href": "/organisations/insolvent/vat/sample/123456789",
      |      "method": "PUT",
      |      "rel": "amend-sample-rel"
      |    }
      |  ]
      |}
    """.stripMargin
  )

  "AmendSampleController" should {
    "return OK" when {
      "happy path" in new Test {

        MockAmendSampleRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockAmendSampleService
          .amendSample(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        val result: Future[Result] = controller.amendSample(vrn)(
          fakePutRequest(requestBodyJson)
        )

        status(result) shouldBe OK
        contentAsJson(result) shouldBe responseBody
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val detail: SampleAuditDetail = SampleAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          vrn = vrn,
          `X-CorrelationId` = correlationId,
          response = SampleAuditResponse(
            httpStatus = OK,
            errors = None
          )
        )

        val event: AuditEvent[SampleAuditDetail] = AuditEvent[SampleAuditDetail](
          auditType = "sampleAuditType",
          transactionName = "sample-transaction-type",
          detail = detail
        )

        MockedAuditService.verifyAuditEvent(event).once
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockAmendSampleRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.amendSample(vrn)(
              fakePutRequest(requestBodyJson)
            )

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val detail: SampleAuditDetail = SampleAuditDetail(
              userType = "Individual",
              agentReferenceNumber = None,
              vrn = vrn,
              `X-CorrelationId` = header("X-CorrelationId", result).get,
              response = SampleAuditResponse(
                httpStatus = expectedStatus,
                errors = Some(Seq(AuditError(error.code)))
              )
            )

            val event: AuditEvent[SampleAuditDetail] = AuditEvent[SampleAuditDetail](
              auditType = "sampleAuditType",
              transactionName = "sample-transaction-type",
              detail = detail
            )

            MockedAuditService.verifyAuditEvent(event).once
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (VrnFormatError, BAD_REQUEST),
          (RuleIncorrectOrEmptyBodyError, BAD_REQUEST),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockAmendSampleRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockAmendSampleService
              .amendSample(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.amendSample(vrn)(
              fakePutRequest(requestBodyJson)
            )

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val detail: SampleAuditDetail = SampleAuditDetail(
              userType = "Individual",
              agentReferenceNumber = None,
              vrn = vrn,
              `X-CorrelationId` = header("X-CorrelationId", result).get,
              response = SampleAuditResponse(
                httpStatus = expectedStatus,
                errors = Some(Seq(AuditError(mtdError.code)))
              )
            )

            val event: AuditEvent[SampleAuditDetail] = AuditEvent[SampleAuditDetail](
              auditType = "sampleAuditType",
              transactionName = "sample-transaction-type",
              detail = detail
            )

            MockedAuditService.verifyAuditEvent(event).once
          }
        }

        val input = Seq(
          (VrnFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}