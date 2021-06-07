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

package v1.connectors

import mocks.MockAppConfig
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.MockHttpClient
import v1.models.domain.Vrn
import v1.models.outcomes.ResponseWrapper
import v1.models.request.{SubmitReturnRequest, SubmitReturnRequestBody}

import scala.concurrent.Future

class SubmitReturnConnectorSpec extends ConnectorSpec {

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

  class Test extends MockHttpClient with MockAppConfig {

    val connector: SubmitReturnConnector = new SubmitReturnConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockAppConfig.desBaseUrl returns baseUrl
    MockAppConfig.desToken returns "des-token"
    MockAppConfig.desEnvironment returns "des-environment"
    MockAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)
  }


  "SubmitReturnConnector" when {
    "submitReturn" should {
      "return correct status upon HttpClient success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))
        implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
        val requiredDesHeadersPost: Seq[(String, String)] = requiredDesHeaders ++ Seq("Content-Type" -> "application/json")

        MockedHttpClient
          .post(
            url = s"$baseUrl/enterprise/return/vat/$vrn",
            body = submitReturnRequest.body,
            config = dummyDesHeaderCarrierConfig,
            requiredHeaders = requiredDesHeadersPost,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          ).returns(Future.successful(outcome))

        await(connector.submitReturn(submitReturnRequest)) shouldBe outcome
      }
    }
  }
}