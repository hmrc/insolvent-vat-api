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
import play.api.libs.json._

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
                                   uniqueId: String,
                                   receivedAt: Option[String] = None,
                                   agentReference: Option[String] = None)

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
      (JsPath \ "uniqueId").read[String] and
      Reads.pure(None) and
      Reads.pure(None)
    ) (SubmitReturnRequestBody.apply _)

  implicit val writes: OWrites[SubmitReturnRequestBody] = new OWrites[SubmitReturnRequestBody] {
    def writes(response: SubmitReturnRequestBody): JsObject = {
      Json.obj("periodKey" -> response.periodKey) ++
        Json.obj("vatDueSales" -> response.vatDueSales) ++
        Json.obj("vatDueAcquisitions" -> response.vatDueAcquisitions) ++
        Json.obj("vatDueTotal" -> response.totalVatDue) ++
        Json.obj("vatReclaimedCurrPeriod" -> response.vatReclaimedCurrPeriod) ++
        Json.obj("vatDueNet" -> response.netVatDue) ++
        Json.obj("totalValueSalesExVAT" -> response.totalValueSalesExVAT) ++
        Json.obj("totalValuePurchasesExVAT" -> response.totalValuePurchasesExVAT) ++
        Json.obj("totalValueGoodsSuppliedExVAT" -> response.totalValueGoodsSuppliedExVAT) ++
        Json.obj("totalAllAcquisitionsExVAT" -> response.totalAcquisitionsExVAT) ++
        Json.obj("uniqueID" -> response.uniqueId) ++
        response.agentReference.fold(Json.obj())(value => Json.obj("agentReferenceNumber" -> value)) ++
        response.receivedAt.fold(Json.obj())(value => Json.obj("receivedAt" -> value))
    }
  }
}