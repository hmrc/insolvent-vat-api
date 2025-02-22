/*
 * Copyright 2023 HM Revenue & Customs
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

package definition

import definition.APIStatus.{ALPHA, BETA}
import definition.Versions.VERSION_1
import mocks.MockAppConfig
import support.UnitSpec
import v1.mocks.MockHttpClient

class ApiDefinitionFactorySpec extends UnitSpec {

  class Test extends MockHttpClient with MockAppConfig {
    val apiDefinitionFactory = new ApiDefinitionFactory(mockAppConfig)
    MockAppConfig.apiGatewayContext returns "/organisations/insolvent/vat"
  }

  "definition" when {
    "called" should {
      "return a valid Definition case class" in new Test {
        MockAppConfig.apiStatus returns "BETA"
        MockAppConfig.endpointsEnabled returns true

        private val writeScope = "write:insolvent-vat"

        apiDefinitionFactory.definition shouldBe
          Definition(
            api = APIDefinition(
              name = "Insolvent VAT (MTD)",
              description = "An API for providing VAT data for insolvent traders",
              context = "/organisations/insolvent/vat",
              categories = Seq("VAT_MTD"),
              versions = Seq(
                APIVersion(
                  version = VERSION_1,
                  access = Some(Access("PRIVATE")),
                  status = BETA,
                  endpointsEnabled = true
                )
              ),
              requiresTrust = None
            )
          )
      }
    }
  }

  "buildAPIStatus" when {
    "the 'apiStatus' parameter is present and valid" should {
      "return the correct status" in new Test {
        MockAppConfig.apiStatus returns "BETA"
        apiDefinitionFactory.buildAPIStatus("1.0") shouldBe BETA
      }
    }

    "the 'apiStatus' parameter is present and invalid" should {
      "default to alpha" in new Test {
        MockAppConfig.apiStatus returns "ALPHO"
        apiDefinitionFactory.buildAPIStatus("1.0") shouldBe ALPHA
      }
    }
  }
}
