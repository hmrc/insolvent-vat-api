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

import support.UnitSpec
import v1.models.errors.ReceivedAtFormatError

class DateFormatValidationSpec extends UnitSpec {

  "DateFormatValidation" when {
    "validate" must {
      "return an empty list for a valid date" in {
        DateFormatValidation.validate(
          date = "2020-05-05T12:01:00Z"
        ) shouldBe NoValidationErrors
      }

      "return a ReceivedAtFormatError for an invalid date" in {
        DateFormatValidation.validate(
          date = "28-07-2021"
        ) shouldBe List(ReceivedAtFormatError)
      }

    }
  }
}
