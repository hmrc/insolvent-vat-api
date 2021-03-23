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

import config.AppConfig
import mocks.MockAppConfig
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import play.api.libs.json.{JsObject, Json}
import support.UnitSpec
import v1.models.errors._
import v1.models.request.amendSample.AmendSampleRawData

class AmendSampleValidatorSpec extends UnitSpec {

  private val validNino = "AA123456A"
  private val requestBodyJson = Json.parse(
    """{
      |  "data" : "someData"
      |}
    """.stripMargin)

  class Test extends MockAppConfig {

    val dateTimeFormatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")

    implicit val appConfig: AppConfig = mockAppConfig

    val validator = new AmendSampleValidator()
  }

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in new Test {
        validator.validate(AmendSampleRawData(validNino, requestBodyJson)) shouldBe Nil
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in new Test {
        validator.validate(AmendSampleRawData("A12344A", requestBodyJson)) shouldBe
          List(NinoFormatError)
      }
    }

    "return multiple errors" when {
      "request supplied has multiple errors" in new Test {
        validator.validate(AmendSampleRawData("A12344A", JsObject.empty)) shouldBe
          List(NinoFormatError, RuleIncorrectOrEmptyBodyError)
      }
    }
  }
}
