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

package v1.controllers.requestParsers.validators

import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.AnyContentAsJson
import support.UnitSpec
import v1.controllers.requestParsers.validators.validations.ValueFormatErrorMessages
import v1.models.errors._
import v1.models.request.SubmitReturnRawData

class SubmitReturnValidatorSpec extends UnitSpec with ValueFormatErrorMessages {
  private val validVrn = "123456789"

  private val validRequestBodyJson: JsValue = Json.parse(
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
  )

  private val invalidPeriodKeyRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "periodKey": "period",
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
  )

  private val invalidReceivedAtRequestBodyJson: JsValue = Json.parse(
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
      |  "receivedAt": "datetime",
      |  "uniqueId": "0123456789"
      |}
    """.stripMargin
  )

  private val invalidUniqueIdRequestBodyJson: JsValue = Json.parse(
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
      |  "uniqueId": "0123456789123"
      |}
    """.stripMargin
  )

  private val invalidVatDueSalesRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "periodKey": "18A1",
      |  "vatDueSales": 105.505,
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
  )

  private val allInvalidValueRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "periodKey": "period",
      |  "vatDueSales": 105.505,
      |  "vatDueAcquisitions": -100.453,
      |  "totalVatDue": 5.052,
      |  "vatReclaimedCurrPeriod": 105.154,
      |  "netVatDue": 100.102,
      |  "totalValueSalesExVAT": 300.99,
      |  "totalValuePurchasesExVAT": 300.99,
      |  "totalValueGoodsSuppliedExVAT": 3000.99,
      |  "totalAcquisitionsExVAT": 3000.99,
      |  "receivedAt": "time",
      |  "uniqueId": "0123456789123"
      |}
          """.stripMargin
  )

  private val emptyRequestBodyJson: JsValue = JsObject.empty

  private val nonValidRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |  "periodKey": true,
      |  "vatDueSales": 105.50,
      |  "vatDueAcquisitions": -100.45,
      |  "totalVatDue": 5.05,
      |  "vatReclaimedCurrPeriod": 105.15,
      |  "netVatDue": 100.10,
      |  "totalValueSalesExVAT": 300,
      |  "totalValuePurchasesExVAT": 300,
      |  "totalValueGoodsSuppliedExVAT": 3000,
      |  "totalAcquisitionsExVAT": 3000
      |}
    """.stripMargin
  )

  private val validRawRequestBody = AnyContentAsJson(validRequestBodyJson)
  private val emptyRawRequestBody = AnyContentAsJson(emptyRequestBodyJson)
  private val nonValidRawRequestBody = AnyContentAsJson(nonValidRequestBodyJson)
  private val invalidPeriodKeyRequestBody = AnyContentAsJson(invalidPeriodKeyRequestBodyJson)
  private val invalidReceivedAtRequestBody = AnyContentAsJson(invalidReceivedAtRequestBodyJson)
  private val invalidUniqueIdRequestBody = AnyContentAsJson(invalidUniqueIdRequestBodyJson)
  private val invalidVatDueSalesRequestBody = AnyContentAsJson(invalidVatDueSalesRequestBodyJson)
  private val allInvalidValueRawRequestBody = AnyContentAsJson(allInvalidValueRequestBodyJson)

  val validator: SubmitReturnValidator = new SubmitReturnValidator()

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in {
        validator.validate(SubmitReturnRawData(validVrn, validRawRequestBody)) shouldBe Nil
      }
    }

    "return a VrnFormatError error" when {
      "a invalid VRN is supplied" in {
        validator.validate(SubmitReturnRawData("notAVrn", validRawRequestBody)) shouldBe
          List(VrnFormatError)
      }
    }

    "return a RuleIncorrectOrEmptyBodyError error" when {
      "an empty JSON body is submitted" in {
        validator.validate(SubmitReturnRawData(validVrn, emptyRawRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError)
      }

      "the submitted request body is not in the correct format and mandatory fields are missing" in {
        validator.validate(SubmitReturnRawData(validVrn, nonValidRawRequestBody)) shouldBe
          List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(Seq("/receivedAt", "/periodKey", "/uniqueId").sorted)))
      }
    }

    "return a PeriodKeyFormatError error" when {
      "a request with an invalid period key is supplied" in {
        validator.validate(SubmitReturnRawData(validVrn, invalidPeriodKeyRequestBody)) shouldBe
          List(PeriodKeyFormatError)
      }
    }

    "return a ReceivedAtFormatError error" when {
      "a request with an invalid receivedAt date is supplied" in {
        validator.validate(SubmitReturnRawData(validVrn, invalidReceivedAtRequestBody)) shouldBe
          List(ReceivedAtFormatError)
      }
    }

    "return a UniqueIDFormatError error" when {
      "a request with an invalid unique ID is supplied" in {
        validator.validate(SubmitReturnRawData(validVrn, invalidUniqueIdRequestBody)) shouldBe
          List(UniqueIDFormatError)
      }
    }

    "return a ValueFormatError error (single failure)" when {
      "one field fails value validation (vatDueSales)" in {
        validator.validate(SubmitReturnRawData(validVrn, invalidVatDueSalesRequestBody)) shouldBe
          List(ValueFormatError.copy(
            message = BIG_DECIMAL_MINIMUM_INCLUSIVE,
            paths = Some(Seq("/vatDueSales"))
          ))
      }

      "return multiple errors (multiple failures)" when {
        "multiple fields fail value validation" in {
          validator.validate(SubmitReturnRawData(validVrn, allInvalidValueRawRequestBody)) shouldBe
            List(
              ReceivedAtFormatError,
              ValueFormatError.copy(
                paths = Some(List(
                  "/vatDueSales",
                  "/vatDueAcquisitions",
                  "/totalVatDue",
                  "/vatReclaimedCurrPeriod"
                ).sorted),
                message = BIG_DECIMAL_MINIMUM_INCLUSIVE
              ),
              UniqueIDFormatError,
              ValueFormatError.copy(
                paths = Some(List(
                  "/totalValueSalesExVAT",
                  "/totalValuePurchasesExVAT",
                  "/totalValueGoodsSuppliedExVAT",
                  "/totalAcquisitionsExVAT"
                ).sorted),
                message = BIG_NON_DECIMAL_MINIMUM_INCLUSIVE
              ),
              ValueFormatError.copy(
                paths = Some(List(
                  "/netVatDue"
                )),
                message = ZERO_MINIMUM_INCLUSIVE
              ),
              PeriodKeyFormatError
            )
        }
      }
    }
  }
}
