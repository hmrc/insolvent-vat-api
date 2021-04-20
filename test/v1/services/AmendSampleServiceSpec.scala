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

package v1.services

import uk.gov.hmrc.domain.Vrn
import v1.controllers.EndpointLogContext
import v1.mocks.connectors.MockAmendSampleConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.submit.{SubmitRequest, SubmitRequestBody}

import scala.concurrent.Future

class AmendSampleServiceSpec extends ServiceSpec {

  private val vrn = "123456789"
  private val correlationId = "X-123"

  private val requestBody = SubmitRequestBody(
    periodKey = "AB12",
    vatDueSales = 1000.00,
    vatDueAcquisitions = 2000.00,
    totalVatDue = 3000.00,
    vatReclaimedCurrPeriod = BigDecimal("99999999999.99"),
    netVatDue = BigDecimal("99999999999.99"),
    totalValueSalesExVAT = BigDecimal("9999999999999"),
    totalValuePurchasesExVAT = BigDecimal("9999999999999"),
    totalValueGoodsSuppliedExVAT = BigDecimal("9999999999999"),
    totalAcquisitionsExVAT = BigDecimal("9999999999999"),
    uniqueId = Some("0123456789"),
    receivedAt = None,
    agentReference = None
  )

  private val requestData = SubmitRequest(
    vrn = Vrn(vrn),
    body = requestBody
  )

  trait Test extends MockAmendSampleConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new AmendSampleService(
      connector = mockAmendSampleConnector
    )
  }

  "AmendSampleService" when {
    "amendSample" must {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockAmendSampleConnector.amendSample(requestData)
          .returns(Future.successful(outcome))

        await(service.amendSample(requestData)) shouldBe outcome
      }
    }

    "map errors according to spec" when {

      def serviceError(desErrorCode: String, error: MtdError): Unit =
        s"a $desErrorCode error is returned from the service" in new Test {

          MockAmendSampleConnector.amendSample(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

          await(service.amendSample(requestData)) shouldBe Left(ErrorWrapper(Some(correlationId), error))
        }

      val input = Seq(
        ("INVALID_VRN", VrnFormatError),
        ("NOT_FOUND", NotFoundError),
        ("SERVER_ERROR", DownstreamError),
        ("SERVICE_UNAVAILABLE", DownstreamError)
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }
}
