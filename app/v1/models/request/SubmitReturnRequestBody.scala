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
  implicit val reads: Reads[SubmitReturnRequestBody] = (
    (JsPath \ "periodKey").read[String] and
      (JsPath \ "vatDueSales").read[BigDecimal] and
      (JsPath \ "vatDueAcquisitions").read[BigDecimal] and
      (JsPath \ "totalVatDue").read[BigDecimal] and
      (JsPath \ "vatReclaimedCurrPeriod").read[BigDecimal] and
      (JsPath \ "netVatDue").read[BigDecimal] and
      (JsPath \ "totalValueSalesExVAT").read[BigDecimal] and
      (JsPath \ "totalValuePurchasesExVAT").read[BigDecimal] and
      (JsPath \ "totalValueGoodsSuppliedExVAT").read[BigDecimal] and
      (JsPath \ "totalAcquisitionsExVAT").read[BigDecimal] and
      (JsPath \ "receivedAt").read[String] and
      (JsPath \ "uniqueId").read[String]
    ) (SubmitReturnRequestBody.apply _)

  implicit val writes: OWrites[SubmitReturnRequestBody] = new OWrites[SubmitReturnRequestBody] {
    def writes(requestBody: SubmitReturnRequestBody): JsObject = {
      Json.obj("periodKey" -> requestBody.periodKey,
        "vatDueSales" -> requestBody.vatDueSales,
        "vatDueAcquisitions" -> requestBody.vatDueAcquisitions,
        "vatDueTotal" -> requestBody.totalVatDue,
        "vatReclaimedCurrPeriod" -> requestBody.vatReclaimedCurrPeriod,
        "vatDueNet" -> requestBody.netVatDue,
        "totalValueSalesExVAT" -> requestBody.totalValueSalesExVAT,
        "totalValuePurchasesExVAT" -> requestBody.totalValuePurchasesExVAT,
        "totalValueGoodsSuppliedExVAT" -> requestBody.totalValueGoodsSuppliedExVAT,
        "totalAllAcquisitionsExVAT" -> requestBody.totalAcquisitionsExVAT,
        "receivedAt" -> requestBody.receivedAt,
        "uniqueID" -> requestBody.uniqueId)
    }
  }
}