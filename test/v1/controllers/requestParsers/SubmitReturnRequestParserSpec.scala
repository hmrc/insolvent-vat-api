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

package v1.controllers.requestParsers

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContentAsJson
import support.UnitSpec
import v1.mocks.validators.MockSubmitReturnValidator
import v1.models.domain.Vrn
import v1.models.errors._
import v1.models.request.{SubmitReturnRawData, SubmitReturnRequest, SubmitReturnRequestBody}

class SubmitReturnRequestParserSpec extends UnitSpec {
  val vrn: String = "123456789"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  private val submitVatReturnRequestJson: JsValue = Json.parse(
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

  private val validRawRequestBody = AnyContentAsJson(submitVatReturnRequestJson)

  private val fullSubmitVatReturnRequestModel = SubmitReturnRequestBody(
    periodKey = "18A1",
    vatDueSales = 105.50,
    vatDueAcquisitions = -100.45,
    totalVatDue = 5.05,
    vatReclaimedCurrPeriod = 105.15,
    netVatDue = 100.10,
    totalValueSalesExVAT = 300,
    totalValuePurchasesExVAT = 300,
    totalValueGoodsSuppliedExVAT = 3000,
    totalAcquisitionsExVAT = 3000,
    receivedAt = "2020-05-05T12:01:00Z",
    uniqueId = "0123456789"
  )

  private val submitReturnRawData = SubmitReturnRawData(
    vrn = vrn,
    body = validRawRequestBody
  )

  trait Test extends MockSubmitReturnValidator {
    lazy val parser: SubmitReturnRequestParser = new SubmitReturnRequestParser(
      validator = mockSubmitReturnValidator
    )
  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockSubmitReturnValidator.validate(submitReturnRawData).returns(Nil)

        parser.parseRequest(submitReturnRawData) shouldBe
          Right(SubmitReturnRequest(Vrn(vrn), fullSubmitVatReturnRequestModel))
      }
    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockSubmitReturnValidator.validate(submitReturnRawData.copy(vrn = "notAVrn"))
          .returns(List(VrnFormatError))

        parser.parseRequest(submitReturnRawData.copy(vrn = "notAVrn")) shouldBe
          Left(ErrorWrapper(correlationId, VrnFormatError, None))
      }

      "multiple field value validation errors occur" in new Test {

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

        private val allInvalidValueRawRequestBody = AnyContentAsJson(allInvalidValueRequestBodyJson)

        private val allInvalidValueErrors = List(
          ReceivedAtFormatError,
          ValueFormatError.copy(
            paths = Some(List(
              "/vatDueSales",
              "/vatDueAcquisitions",
              "/totalVatDue",
              "/vatReclaimedCurrPeriod"
            )),
            message = "The field should be between -9999999999999.99 and 9999999999999.99"
          ),
          UniqueIDFormatError,
          ValueFormatError.copy(
            paths = Some(List(
              "/totalValueSalesExVAT",
              "/totalValuePurchasesExVAT",
              "/totalValueGoodsSuppliedExVAT",
              "/totalAcquisitionsExVAT"
            )),
            message = "The field should be between -9999999999999 and 9999999999999"
          ),
          ValueFormatError.copy(
            paths = Some(List(
              "/netVatDue"
            )),
            message = "The field should be between 0.00 and 99999999999.99"
          ),
          PeriodKeyFormatError
        )

        MockSubmitReturnValidator.validate(submitReturnRawData.copy(body = allInvalidValueRawRequestBody))
          .returns(allInvalidValueErrors)
      }
    }
  }
}
