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

package v1.models.response

import org.joda.time.DateTime
import play.api.libs.json.{JsError, JsObject, JsValue, Json}
import support.UnitSpec

class SubmitReturnResponseSpec extends UnitSpec {

  val fullDesJson: JsValue = Json.parse(
    """
      |{
      |  "processingDate": "2021-03-16T08:20:27.895Z",
      |  "formBundleNumber": "256660290587",
      |  "paymentIndicator": "BANK",
      |  "chargeRefNumber": "aCxFaNx0FZsCvyWF"
      |}
    """.stripMargin
  )

  val minDesJson: JsValue = Json.parse(
    """
      |{
      |  "processingDate": "2021-03-16T08:20:27.895Z"
      |}
    """.stripMargin
  )

  val noMilliDesJson: JsValue = Json.parse(
    """
      |{
      |  "processingDate": "2021-03-16T08:20:27.89Z"
      |}
    """.stripMargin
  )

  val fullMtdJson: JsValue = Json.parse(
    """
      |{
      |  "processingDate": "2021-03-16T08:20:27.895Z",
      |  "formBundleNumber": "256660290587",
      |  "paymentIndicator": "BANK",
      |  "chargeRefNumber": "aCxFaNx0FZsCvyWF"
      |}
    """.stripMargin
  )

  val minMtdJson: JsValue = Json.parse(
    """
      |{
      |  "processingDate": "2021-03-16T08:20:27.895Z"
      |}
    """.stripMargin
  )

  val noMilliMtdJson: JsValue = Json.parse(
    """
      |{
      |  "processingDate": "2021-03-16T08:20:27.890Z"
      |}
    """.stripMargin
  )

  val fullResponseModel: SubmitReturnResponse = SubmitReturnResponse(
    processingDate = new DateTime("2021-03-16T08:20:27.895+0000"),
    formBundleNumber = Some("256660290587"),
    paymentIndicator = Some("BANK"),
    chargeRefNumber = Some("aCxFaNx0FZsCvyWF")
  )

  val minResponseModel: SubmitReturnResponse = SubmitReturnResponse(
    processingDate = new DateTime("2021-03-16T08:20:27.895+0000"),
    formBundleNumber = None,
    paymentIndicator = None,
    chargeRefNumber = None
  )

  val noMilliResponseModel: SubmitReturnResponse = SubmitReturnResponse(
    processingDate = new DateTime("2021-03-16T08:20:27.89Z"),
    formBundleNumber = None,
    paymentIndicator = None,
    chargeRefNumber = None
  )

  "SubmitReturnResponse" should {
    "return a SubmitReturnResponse model" when {
      "only mandatory fields are provided" in {
        minDesJson.as[SubmitReturnResponse] shouldBe minResponseModel
      }

      "all fields are provided" in {
        fullDesJson.as[SubmitReturnResponse] shouldBe fullResponseModel
      }

      "the processing date is provided without milliseconds" in {
        noMilliDesJson.as[SubmitReturnResponse] shouldBe noMilliResponseModel
      }
    }

    "return a JsError" when {
      "an invalid json is provided" in {
        val invalidJson = JsObject.empty
        invalidJson.validate[SubmitReturnResponse] shouldBe a[JsError]
      }
    }

    "write to json" when {
      "a full model is provided" in {
        Json.toJson(fullResponseModel) shouldBe fullMtdJson
      }

      "a model with only mandatory fields is provided" in {
        Json.toJson(minResponseModel) shouldBe minMtdJson
      }

      "the processing date has been provided without milliseconds" in {
        Json.toJson(noMilliResponseModel) shouldBe noMilliMtdJson
      }
    }
  }
}
