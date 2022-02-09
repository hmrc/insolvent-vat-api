/*
 * Copyright 2022 HM Revenue & Customs
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

package v1.models.audit

import play.api.http.Status.{BAD_REQUEST, CREATED}
import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v1.models.errors.VrnFormatError

class GenericAuditDetailSpec extends UnitSpec {

  val vrn: String = "123456789"
  val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  val auditDetailJsonSuccess: JsValue = Json.parse(
    s"""
      |{
      |    "vrn": "$vrn",
      |    "request": {
      |      "periodKey": "18A1",
      |      "vatDueSales": 105.50,
      |      "vatDueAcquisitions": -100.45,
      |      "totalVatDue": 5.05,
      |      "vatReclaimedCurrPeriod": 105.15,
      |      "netVatDue": 100.10,
      |      "totalValueSalesExVAT": 300,
      |      "totalValuePurchasesExVAT": 300,
      |      "totalValueGoodsSuppliedExVAT": 3000,
      |      "totalAcquisitionsExVAT": 3000,
      |      "receivedAt": "2020-05-05T12:01:00Z",
      |      "uniqueId": "0123456789"
      |    },
      |    "X-CorrelationId": "$correlationId",
      |    "response": {
      |      "httpStatus": $CREATED
      |    }
      |}
    """.stripMargin
  )

  val auditDetailModelSuccess: GenericAuditDetail = GenericAuditDetail(
    params = Map("vrn" -> vrn),
    request = Some(Json.parse(
      """
        |{
        |  "periodKey": "18A1",
        |  "vatDueSales": 105.50,
        |  "vatDueAcquisitions": -100.45,
        |  "totalVatDue": 5.05,
        |  "vatReclaimedCurrPeriod": 105.15,
        |  "netVatDue": 100.10,
        |  "totalValueSalesExVAT": 300,
        |  "totalValuePurchasesExVAT": 300,
        |  "totalValueGoodsSuppliedExVAT": 3000,
        |  "totalAcquisitionsExVAT": 3000,
        |  "receivedAt": "2020-05-05T12:01:00Z",
        |  "uniqueId": "0123456789"
        |}
        """.stripMargin
    )),
    `X-CorrelationId` = correlationId,
    response = AuditResponse(
      httpStatus = CREATED,
      response = Right(None)
    )
  )

  val invalidTaxYearAuditDetailJson: JsValue = Json.parse(
    s"""
      |{
      |    "vrn": "$vrn",
      |    "request": {
      |      "periodKey": "18A1",
      |      "vatDueSales": 105.50,
      |      "vatDueAcquisitions": -100.45,
      |      "totalVatDue": 5.05,
      |      "vatReclaimedCurrPeriod": 105.15,
      |      "netVatDue": 100.10,
      |      "totalValueSalesExVAT": 300,
      |      "totalValuePurchasesExVAT": 300,
      |      "totalValueGoodsSuppliedExVAT": 3000,
      |      "totalAcquisitionsExVAT": 3000,
      |      "receivedAt": "2020-05-05T12:01:00Z",
      |      "uniqueId": "0123456789"
      |    },
      |    "X-CorrelationId": "$correlationId",
      |    "response": {
      |      "httpStatus": $BAD_REQUEST,
      |      "errors": [
      |        {
      |          "errorCode": "FORMAT_VRN"
      |        }
      |      ]
      |    }
      |}
    """.stripMargin
  )

  val invalidTaxYearAuditDetailModel: GenericAuditDetail = GenericAuditDetail(
    params = Map("vrn" -> vrn),
    request = Some(Json.parse(
      """
        |{
        |  "periodKey": "18A1",
        |  "vatDueSales": 105.50,
        |  "vatDueAcquisitions": -100.45,
        |  "totalVatDue": 5.05,
        |  "vatReclaimedCurrPeriod": 105.15,
        |  "netVatDue": 100.10,
        |  "totalValueSalesExVAT": 300,
        |  "totalValuePurchasesExVAT": 300,
        |  "totalValueGoodsSuppliedExVAT": 3000,
        |  "totalAcquisitionsExVAT": 3000,
        |  "receivedAt": "2020-05-05T12:01:00Z",
        |  "uniqueId": "0123456789"
        |}
      """.stripMargin
    )),
    `X-CorrelationId` = correlationId,
    response = AuditResponse(BAD_REQUEST, Left(Seq(AuditError(VrnFormatError.code))))
  )

  "GenericAuditDetail" when {
    "written to JSON (success)" should {
      "produce the expected JsObject" in {
        Json.toJson(auditDetailModelSuccess) shouldBe auditDetailJsonSuccess
      }
    }

    "written to JSON (error)" should {
      "produce the expected JsObject" in {
        Json.toJson(invalidTaxYearAuditDetailModel) shouldBe invalidTaxYearAuditDetailJson
      }
    }
  }
}