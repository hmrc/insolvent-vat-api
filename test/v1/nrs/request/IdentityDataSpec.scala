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

package v1.nrs.request

import org.joda.time.{DateTime, DateTimeZone, LocalDate}
import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.auth.core.{ConfidenceLevel, User}
import uk.gov.hmrc.auth.core.retrieve.{AgentInformation, Credentials, ItmpAddress, ItmpName, LoginTimes, MdtpInformation, Name}
import v1.nrs.models.request.IdentityData

class IdentityDataSpec extends UnitSpec {

  val correctModel: IdentityData = IdentityData(
    internalId = Some("some-id"),
    externalId = Some("some-id"),
    agentCode = Some("TZRXXV"),
    credentials = Some(Credentials("12345-credId", "GovernmentGateway")),
    confidenceLevel = ConfidenceLevel.L200,
    nino = Some("DH00475D"),
    saUtr = Some("Utr"),
    name = Some(Name(Some("test"), Some("test"))),
    dateOfBirth = Some(LocalDate.parse("1985-01-01")),
    email = Some("test@test.com"),
    agentInformation = AgentInformation(agentCode = Some("TZRXXV"), agentFriendlyName = Some("Bodgitt & Legget LLP"), agentId = Some("BDGL")),
    groupIdentifier = Some("GroupId"),
    credentialRole = Some(User),
    mdtpInformation = Some(MdtpInformation("DeviceId", "SessionId")),
    itmpName = ItmpName(None, None, None),
    itmpDateOfBirth = None,
    itmpAddress = ItmpAddress(None, None, None, None, None, None, None, None),
    affinityGroup = Some(Agent),
    credentialStrength = Some("strong"),
    loginTimes = LoginTimes(
      DateTime.parse("2016-11-27T09:00:00Z").withZone(DateTimeZone.UTC),
      Some(DateTime.parse("2016-11-01T12:00:00Z").withZone(DateTimeZone.UTC))
    )
  )

  val correctJson: JsValue = Json.parse(
    """
      |{
      |  "internalId": "some-id",
      |  "externalId": "some-id",
      |  "agentCode": "TZRXXV",
      |  "credentials": {"providerId": "12345-credId",
      |  "providerType": "GovernmentGateway"},
      |  "confidenceLevel": 200,
      |  "nino": "DH00475D",
      |  "saUtr": "Utr",
      |  "name": { "name": "test", "lastName": "test" },
      |  "dateOfBirth": "1985-01-01",
      |  "email":"test@test.com",
      |  "agentInformation": {
      |    "agentId": "BDGL",
      |    "agentCode" : "TZRXXV",
      |    "agentFriendlyName" : "Bodgitt & Legget LLP"
      |  },
      |  "groupIdentifier" : "GroupId",
      |  "credentialRole": "User",
      |  "mdtpInformation" : {"deviceId" : "DeviceId",
      |    "sessionId": "SessionId" },
      |  "itmpName" : {},
      |  "itmpAddress" : {},
      |  "affinityGroup": "Agent",
      |  "credentialStrength": "strong",
      |  "loginTimes": {
      |    "currentLogin": "2016-11-27T09:00:00Z",
      |    "previousLogin": "2016-11-01T12:00:00Z"
      |  }
      |}
   """.stripMargin
  )

  "writes" should {
    "parse correctly to json" in {
      Json.toJson(correctModel) shouldBe correctJson
    }
  }
}
