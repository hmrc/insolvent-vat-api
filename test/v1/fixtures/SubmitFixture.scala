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

package v1.fixtures

import play.api.libs.json.{JsObject, JsValue, Json}

object SubmitFixture {

    val requestBodyJson = Json.parse(
    """{
      |     "periodKey" : "AB12",
      |    "vatDueSales" : 1000.00,
      |    "vatDueAcquisitions" : 2000.00,
      |    "totalVatDue" : 3000.00,
      |    "vatReclaimedCurrPeriod" : 99999999999.99,
      |    "netVatDue" : 99999999999.99,
      |    "totalValueSalesExVAT" : 9999999999999,
      |    "totalValuePurchasesExVAT" : 9999999999999,
      |    "totalValueGoodsSuppliedExVAT" : 9999999999999,
      |    "totalAcquisitionsExVAT" : 9999999999999,
      |    "uniqueId" : "0123456789"
      |}
    """.stripMargin)

}
