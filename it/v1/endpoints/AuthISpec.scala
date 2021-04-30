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
    //val periodKey = "AB19"

    val json: JsValue = Json.parse(
      """
        |{
        |   "periodKey": 1,
        |   "vatDueSales": 	Invalid Json,
        |   "vatDueAcquisitions": 	3000.00,
        |   "totalVatDue": 	10000,
        |   "vatReclaimedCurrPeriod": 	1000,
        |   "netVatDue": 	9000,
        |   "totalValueSalesExVAT": 	1000,
        |   "totalValuePurchasesExVAT": 	200,
        |   "totalValueGoodsSuppliedExVAT": 	100000,
        |   "totalAcquisitionsExVAT": 	540,
        |   "receivedAt":  "2020-05-05T12:01:00Z",
        |   "uniqueId": "0123456789"
        |}
      """.stripMargin
    )

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      //buildRequest(s"/$vrn/returns/$periodKey") //TODO: Use this endpoint instead once it is built.
      buildRequest(s"/sample/$vrn")
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "Calling the template endpoint" when {
    "the user is authorised" should {
      "return 200" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          DesStub.serviceSuccess(vrn)
        }

        val response: WSResponse = await(request().put(json))
        response.status shouldBe OK
      }
    }

    "the user is belongs to an unsupported affinity group" should {
      "return 500" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.unauthorisedUnsupportedAffinity()
          DesStub.serviceSuccess(vrn)
        }

        val response: WSResponse = await(request().put(json))
        response.status shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "the user is not logged in" should {
      "return 500" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.unauthorisedNotLoggedIn()
        }

        val response: WSResponse = await(request().put(json))
        response.status shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "the user is NOT authorised" should {
      "return 500" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.unauthorisedOther()
        }

        val response: WSResponse = await(request().put(json))
        response.status shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
