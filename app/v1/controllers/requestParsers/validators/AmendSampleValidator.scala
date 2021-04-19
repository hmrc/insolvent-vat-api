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

import v1.controllers.requestParsers.validators.validations._
import v1.models.errors.MtdError
import v1.models.request.submit.{SubmitRawData, SubmitRequestBody}

class AmendSampleValidator extends Validator[SubmitRawData] {

  private val validationSet = List(parameterFormatValidation, parameterRuleValidation)

  private def parameterFormatValidation: SubmitRawData => List[List[MtdError]] = (data: SubmitRawData) => {
    List(
      VrnValidation.validate(data.vrn),
      JsonFormatValidation.validate[SubmitRequestBody](data.body)
    )
  }

  private def parameterRuleValidation: SubmitRawData => List[List[MtdError]] = { data =>
    List(
    )
  }

  override def validate(data: SubmitRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }
}
