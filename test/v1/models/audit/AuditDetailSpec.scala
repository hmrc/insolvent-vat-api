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

package v1.models.audit

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class AuditDetailSpec extends UnitSpec {

  private val userType = "Organisation"
  private val agentReferenceNumber = Some("012345678")
  private val vrn = "123456789"
  private val `X-CorrelationId` = "X-123"

  "writes" when {
    "passed an audit model with all fields provided" should {
      "produce valid json" in {
        val json = Json.parse(
          s"""
             |{
             |  "userType": "Organisation",
             |  "agentReferenceNumber": "012345678",
             |  "vrn": "123456789",
             |  "X-CorrelationId": "X-123",
             |  "response": {
             |    "httpStatus": 303
             |  }
             |}
           """.stripMargin)

        val model = SampleAuditDetail(userType, agentReferenceNumber, vrn, `X-CorrelationId`, responseSuccess)

        Json.toJson(model) shouldBe json
      }
    }

    "passed an audit model with only mandatory fields provided" should {
      "produce valid json" in {
        val json = Json.parse(
          s"""
             |{
             |  "userType": "Organisation",
             |  "vrn": "123456789",
             |  "X-CorrelationId": "X-123",
             |  "response": {
             |    "httpStatus": 400,
             |    "errors": [
             |      {
             |        "errorCode": "FORMAT_VRN"
             |      }
             |    ]
             |  }
             |}
           """.stripMargin)

        val model = SampleAuditDetail(userType, None, vrn, `X-CorrelationId`, responseFail)

        Json.toJson(model) shouldBe json
      }
    }
  }
}
