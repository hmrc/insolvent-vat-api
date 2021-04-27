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

import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, _}

case class SubmitReturnRequestBody(periodKey: String,
                                   vatDueSales: BigDecimal,
                                   vatDueAcquisitions: BigDecimal,
                                   totalVatDue: BigDecimal,
                                   vatReclaimedCurrPeriod: BigDecimal,
                                   netVatDue: BigDecimal,
                                   totalValueSalesExVAT: BigDecimal,
                                   totalValuePurchasesExVAT: BigDecimal,
                                   totalValueGoodsSuppliedExVAT: BigDecimal,
                                   totalAcquisitionsExVAT: BigDecimal,
                                   receivedAt: String,
                                   uniqueId: String)

object SubmitReturnRequestBody {
  implicit val reads: Reads[SubmitReturnRequestBody] = Json.reads[SubmitReturnRequestBody]

  implicit val writes: OWrites[SubmitReturnRequestBody] = (
    (JsPath \ "periodKey").write[String] and
      (JsPath \ "vatDueSales").write[BigDecimal] and
      (JsPath \ "vatDueAcquisitions").write[BigDecimal] and
      (JsPath \ "vatDueTotal").write[BigDecimal] and
      (JsPath \ "vatReclaimedCurrPeriod").write[BigDecimal] and
      (JsPath \ "vatDueNet").write[BigDecimal] and
      (JsPath \ "totalValueSalesExVAT").write[BigDecimal] and
      (JsPath \ "totalValuePurchasesExVAT").write[BigDecimal] and
      (JsPath \ "totalValueGoodsSuppliedExVAT").write[BigDecimal] and
      (JsPath \ "totalAllAcquisitionsExVAT").write[BigDecimal] and
      (JsPath \ "receivedAt").write[String] and
      (JsPath \ "uniqueID").write[String]
    ) (unlift(SubmitReturnRequestBody.unapply))
}