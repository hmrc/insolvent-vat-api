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


import v1.controllers.EndpointLogContext
import v1.mocks.connectors.MockSubmitReturnConnector
import v1.models.domain.Vrn
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.{SubmitReturnRequest, SubmitReturnRequestBody}

import scala.concurrent.Future

class SubmitReturnServiceSpec extends ServiceSpec {

  val vrn: String = "123456789"

  val submitReturnRequestBody: SubmitReturnRequestBody = SubmitReturnRequestBody(
    periodKey = "18A1",
    vatDueSales = 1000.00,
    vatDueAcquisitions = 2000.00,
    totalVatDue = 3000.00,
    vatReclaimedCurrPeriod = 1500.00,
    netVatDue = 1500.00,
    totalValueSalesExVAT = 999999999.00,
    totalValuePurchasesExVAT = 999999999.00,
    totalValueGoodsSuppliedExVAT = 999999999.00,
    totalAcquisitionsExVAT = 999999999.00,
    uniqueId = "0123456789",
    receivedAt = "2021-05-05T12:01:00Z"
  )

  val submitReturnRequest: SubmitReturnRequest = SubmitReturnRequest(
    vrn = Vrn(vrn),
    body = submitReturnRequestBody
  )

  trait Test extends MockSubmitReturnConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service: SubmitReturnService = new SubmitReturnService(
      connector = mockSubmitReturnConnector
    )
  }

  "SubmitReturnService" when {
    "submitReturn" must {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockSubmitReturnConnector.submitReturn(submitReturnRequest)
          .returns(Future.successful(outcome))

        await(service.submitReturn(submitReturnRequest)) shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockSubmitReturnConnector.submitReturn(submitReturnRequest)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

            await(service.submitReturn(submitReturnRequest)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input: Seq[(String, MtdError)] = Seq(
          ("INVALID_VRN", VrnFormatError),
          ("INVALID_ORIGINATOR_ID", DownstreamError),
          ("INVALID_PAYLOAD", DownstreamError),
          ("INVALID_SUBMISSION", DownstreamError),
          ("INVALID_PERIODKEY", PeriodKeyFormatError),
          ("INSOLVENT_TRADER", DownstreamError),
          ("DUPLICATE_SUBMISSION", DownstreamError),
          ("TAX_PERIOD_NOT_ENDED", DownstreamError),
          ("NOT_FOUND_VRN", DownstreamError),
          ("SERVER_ERROR", DownstreamError),
          ("SERVICE_UNAVAILABLE", DownstreamError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }
}