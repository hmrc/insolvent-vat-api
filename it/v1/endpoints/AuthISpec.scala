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

package v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec
import v1.stubs.{AuditStub, AuthStub, DesStub}

class AuthISpec extends IntegrationBaseSpec {

  private trait Test {
    val vrn = "123456789"

    val desResponse: JsValue = Json.parse(
      """
        |{
        |  "processingDate": "2017-10-18T00:01:00Z",
        |  "formBundleNumber": "123456789012",
        |  "paymentIndicator": "DD",
        |  "chargeRefNumber": "SKDJGFH9URGT"
        |}
      """.stripMargin
    )

    val json: JsValue = Json.parse(
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

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(s"/$vrn/returns")
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "Calling the Insolvent Submit VAT Return endpoint" when {
    "the user is authorised" should {
      "return 200" in new Test {

        def desUrl: String = s"/enterprise/return/vat/$vrn"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          DesStub.onSuccess(DesStub.POST, desUrl, OK, desResponse)
        }

        val response: WSResponse = await(request().post(json))
        response.status shouldBe CREATED
      }
    }

    "the user is belongs to an unsupported affinity group" should {
      "return 500" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.unauthorisedUnsupportedAffinity()
          DesStub.serviceSuccess(vrn)
        }

        val response: WSResponse = await(request().post(json))
        response.status shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "the user is not logged in" should {
      "return 500" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.unauthorisedNotLoggedIn()
        }

        val response: WSResponse = await(request().post(json))
        response.status shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "the user is NOT authorised" should {
      "return 500" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.unauthorisedOther()
        }

        val response: WSResponse = await(request().post(json))
        response.status shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
