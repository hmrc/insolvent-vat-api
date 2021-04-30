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

package definition

import config.{AppConfig, FeatureSwitch}
import definition.Versions._
import javax.inject.{Inject, Singleton}
import play.api.Logger

@Singleton
class ApiDefinitionFactory @Inject()(appConfig: AppConfig) {

  private val logger: Logger = Logger(this.getClass)

  lazy val definition: Definition =
    Definition(
      scopes = Seq(),
      api = APIDefinition(
        name = "Insolvent VAT (MTD)",
        description = "An API for providing VAT data for insolvent traders",
        context = appConfig.apiGatewayContext,
        categories = Seq("VAT_MTD"),
        versions = Seq(
          APIVersion(
            version = VERSION_1,
            access = buildAllowListAccess(),
            status = buildAPIStatus(VERSION_1),
            endpointsEnabled = appConfig.endpointsEnabled(VERSION_1)
          )
        ),
        requiresTrust = None
      )
    )

  private[definition] def buildAPIStatus(version: String): APIStatus = {
    APIStatus.parser.lift(appConfig.apiStatus(version))
      .getOrElse {
        logger.error(s"[ApiDefinition][buildApiStatus] no API Status found in config.  Reverting to Alpha")
        APIStatus.ALPHA
      }
  }

  private[definition] def buildAllowListAccess(): Option[Access] = {
    val featureSwitch = FeatureSwitch(appConfig.featureSwitch)
    if (featureSwitch.isAllowListEnabled) Some(Access("PRIVATE", featureSwitch.allowListApplicationIds)) else None
  }
}
