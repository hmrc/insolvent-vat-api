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

package v1.controllers.requestParsers.validators.validations

import v1.models.errors.{MtdError, ValueFormatError}

object NonDecimalValueValidation extends ValueFormatErrorMessages {

  def validate(amount: BigDecimal,
               minValue: BigDecimal = -9999999999999.0,
               maxValue: BigDecimal = 9999999999999.0,
               path: String,
               message: String = BIG_NONDECIMAL_MINIMUM_INCLUSIVE): List[MtdError] = {

    val rangeCheck = !(amount > maxValue || amount < minValue)

    if (rangeCheck && amount.isWhole()) NoValidationErrors else List(
      ValueFormatError.copy(
        message = message,
        paths = Some(Seq(path))
      )
    )
  }
}
