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

package v1.models.request

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v1.models.request.submit.SubmitRequestBody
import v1.fixtures.SubmitFixture._

class SubmitRequestBodySpec extends UnitSpec {

  val toDesJson: JsValue = Json.parse(
    """
      |{
      |   "vatDueAcquisitions": 	2000,
      |   "vatDueSales": 	1000,
      |   "totalValuePurchasesExVAT":  9999999999999,
      |   "totalAllAcquisitionsExVAT": 	9999999999999,
      |   "agentReferenceNumber":"LARN0085901",
      |   "periodKey": "AB12",
      |   "vatDueNet": 	99999999999.99,
      |   "totalValueSalesExVAT": 	9999999999999,
      |   "receivedAt":"2020-05-05T12:00:00Z",
      |   "vatReclaimedCurrPeriod": 	99999999999.99,
      |   "vatDueTotal": 	3000,
      |   "totalValueGoodsSuppliedExVAT": 	9999999999999
      |}
    """.stripMargin)

  val submitRequestBody: SubmitRequestBody = SubmitRequestBody("AB12", BigDecimal(1000.00),
    BigDecimal(2000.00), BigDecimal(3000.00), BigDecimal(99999999999.99),
    BigDecimal(99999999999.99), BigDecimal(9999999999999.00), BigDecimal(9999999999999.00),
    BigDecimal(9999999999999.00), BigDecimal(9999999999999.00), Some("0123456789"), None, None)

  val submitRequestToDesBody: SubmitRequestBody = SubmitRequestBody("AB12", BigDecimal(1000.00),
    BigDecimal(2000.00), BigDecimal(3000.00), BigDecimal(99999999999.99),
    BigDecimal(99999999999.99), BigDecimal(9999999999999.00), BigDecimal(9999999999999.00),
    BigDecimal(9999999999999.00), BigDecimal(9999999999999.00), None, Some("2020-05-05T12:00:00Z"), Some("LARN0085901"))

  "Submit request body" should {
    "return a SubmitRequestBody model" when {
      "valid json is provided" in {

        requestBodyJson.as[SubmitRequestBody] shouldBe submitRequestBody
      }
    }

    "write valid Json" when {
      "a valid model is provided" in {

        Json.toJson(submitRequestToDesBody) shouldBe toDesJson
      }
    }
  }
}
