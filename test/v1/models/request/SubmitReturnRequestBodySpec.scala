/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.libs.json.{JsError, JsObject, JsValue, Json}
import support.UnitSpec

class SubmitReturnRequestBodySpec extends UnitSpec {

  val mtdJson: JsValue = Json.parse(
    """
      |{
      |   "periodKey": "18A1",
      |   "vatDueSales": 1000.00,
      |   "vatDueAcquisitions": 2000.00,
      |   "totalVatDue": 3000.00,
      |   "vatReclaimedCurrPeriod": 1500.00,
      |   "netVatDue": 1500.00,
      |   "totalValueSalesExVAT": 999999999.00,
      |   "totalValuePurchasesExVAT": 999999999.00,
      |   "totalValueGoodsSuppliedExVAT": 999999999.00,
      |   "totalAcquisitionsExVAT": 999999999.00,
      |   "receivedAt": "2020-05-05T12:01:00Z",
      |   "uniqueId": "0123456789"
      |}
    """.stripMargin
  )

  val desJson: JsValue = Json.parse(
    """
      |{
      |   "periodKey": "18A1",
      |   "vatDueSales": 1000.00,
      |   "vatDueAcquisitions": 2000.00,
      |   "vatDueTotal": 3000.00,
      |   "vatReclaimedCurrPeriod": 1500.00,
      |   "vatDueNet": 1500.00,
      |   "totalValueSalesExVAT": 999999999.00,
      |   "totalValuePurchasesExVAT": 999999999.00,
      |   "totalValueGoodsSuppliedExVAT": 999999999.00,
      |   "totalAllAcquisitionsExVAT": 999999999.00,
      |   "receivedAt": "2020-05-05T12:01:00Z",
      |   "uniqueID": "0123456789"
      |}
    """.stripMargin
  )

  val requestBodyModel: SubmitReturnRequestBody = SubmitReturnRequestBody(
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
    receivedAt = "2020-05-05T12:01:00Z"
  )

  val requestBodyToDesModel: SubmitReturnRequestBody = SubmitReturnRequestBody(
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
    receivedAt = "2020-05-05T12:01:00Z"
  )

  "SubmitReturnRequestBody" should {
    "return a SubmitReturnRequestBody model" when {
      "a valid json is provided" in {
        mtdJson.as[SubmitReturnRequestBody] shouldBe requestBodyModel
      }
    }

    "return a JsError" when {
      "an invalid json is provided" in {
        val invalidJson = JsObject.empty
        invalidJson.validate[SubmitReturnRequestBody] shouldBe a[JsError]
      }
    }

    "write valid Json" when {
      "a full valid model is provided" in {
        Json.toJson(requestBodyToDesModel) shouldBe desJson
      }
    }
  }
}