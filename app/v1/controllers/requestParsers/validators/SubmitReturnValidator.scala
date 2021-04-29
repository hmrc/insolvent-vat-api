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

package v1.controllers.requestParsers.validators

import v1.controllers.requestParsers.validators.validations.{DateTimeFormatValidation, DecimalValueValidation, JsonFormatValidation, NonDecimalValueValidation, PeriodKeyValidation, UniqueIDValidation, ValueFormatErrorMessages, VrnValidation}
import v1.models.errors.MtdError
import v1.models.request.{SubmitReturnRawData, SubmitReturnRequestBody}

class SubmitReturnValidator extends Validator[SubmitReturnRawData] with ValueFormatErrorMessages{

  private val validationSet = List(parameterFormatValidation, bodyFormatValidation, bodyValueValidator)

  private def parameterFormatValidation: SubmitReturnRawData => List[List[MtdError]] = (data: SubmitReturnRawData) => {
    List(
      VrnValidation.validate(data.vrn)
    )
  }

  private def bodyFormatValidation: SubmitReturnRawData => List[List[MtdError]] = (data: SubmitReturnRawData) => {
    List(
      JsonFormatValidation.validate[SubmitReturnRequestBody](data.body.json)
    )
  }

  private def bodyValueValidator: SubmitReturnRawData => List[List[MtdError]] = { data =>
    val requestBodyData = data.body.json.as[SubmitReturnRequestBody]

    List(Validator.flattenErrors(List(
      PeriodKeyValidation.validate(requestBodyData.periodKey),
      DecimalValueValidation.validate(
        amount = requestBodyData.vatDueSales,
        path = "/vatDueSales",
        minValue = -9999999999999.99,
      ),
      DecimalValueValidation.validate(
        amount = requestBodyData.vatDueAcquisitions,
        path = "/vatDueAcquisitions",
        minValue = -9999999999999.99,
      ),
      DecimalValueValidation.validate(
        amount = requestBodyData.totalVatDue,
        path = "/totalVatDue",
        minValue = -9999999999999.99,
      ),
      DecimalValueValidation.validate(
        amount = requestBodyData.vatReclaimedCurrPeriod,
        path = "/vatReclaimedCurrPeriod",
        minValue = -9999999999999.99,
      ),
      DecimalValueValidation.validate(
        amount = requestBodyData.netVatDue,
        path = "/netVatDue",
        maxValue = 99999999999.99,
        message = ZERO_MINIMUM_INCLUSIVE
      ),
      NonDecimalValueValidation.validate(
        amount = requestBodyData.totalValueSalesExVAT,
        path = "/totalValueSalesExVAT"
      ),
      NonDecimalValueValidation.validate(
        amount = requestBodyData.totalValuePurchasesExVAT,
        path = "/totalValuePurchasesExVAT"
      ),
      NonDecimalValueValidation.validate(
        amount = requestBodyData.totalValueGoodsSuppliedExVAT,
        path = "/totalValueGoodsSuppliedExVAT"
      ),
      NonDecimalValueValidation.validate(
        amount = requestBodyData.totalAcquisitionsExVAT,
        path = "/totalAcquisitionsExVAT"
      ),
      DateTimeFormatValidation.validate(requestBodyData.receivedAt),
      UniqueIDValidation.validate(requestBodyData.uniqueId)
    )))
  }

  override def validate(data: SubmitReturnRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }
}
