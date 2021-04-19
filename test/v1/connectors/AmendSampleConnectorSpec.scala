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
import uk.gov.hmrc.domain.Vrn
import v1.mocks.MockHttpClient
import v1.models.outcomes.ResponseWrapper
import v1.models.request.submit.{SubmitRequest, SubmitRequestBody}

import scala.concurrent.Future

class AmendSampleConnectorSpec extends ConnectorSpec {

  val vrn: String = "123456789"

  val request: SubmitRequest = SubmitRequest(
    vrn = Vrn(vrn),
    body = SubmitRequestBody(
      periodKey = "AB12",
      vatDueSales = 1000.00,
      vatDueAcquisitions = 	2000.00,
      totalVatDue = 	3000.00,
      vatReclaimedCurrPeriod = 	BigDecimal("99999999999.99"),
      netVatDue =  BigDecimal("99999999999.99"),
      totalValueSalesExVAT = 	BigDecimal("9999999999999"),
      totalValuePurchasesExVAT = 	BigDecimal("9999999999999"),
      totalValueGoodsSuppliedExVAT = 	BigDecimal("9999999999999"),
      totalAcquisitionsExVAT = 	BigDecimal("9999999999999"),
      uniqueId = Some("0123456789"),
      receivedAt = None,
      agentReference = None
    )
  )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: AmendSampleConnector = new AmendSampleConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    val desRequestHeaders: Seq[(String, String)] = Seq(
      "Environment" -> "des-environment",
      "Authorization" -> s"Bearer des-token"
    )

    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
  }

  "AmendSampleConnector" when {
    "amendSample" must {
      "return a 204 status for a success scenario" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockedHttpClient
          .put(
            url = s"$baseUrl/some-placeholder/template/$vrn",
            body = request.body,
            requiredHeaders = desRequestHeaders: _*
          ).returns(Future.successful(outcome))

        await(connector.amendSample(request)) shouldBe outcome
      }
    }
  }
}
