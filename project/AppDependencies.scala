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

import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val bootstrapVersion = "8.5.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-backend-play-30" % bootstrapVersion,
    "org.typelevel" %% "cats-core"         % "2.10.0",
    "com.chuusai"   %% "shapeless"         % "2.4.0-M1",
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"               %  "2.17.0"

  )

  def test: Seq[sbt.ModuleID] = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-30" % bootstrapVersion,
    "org.scalatest"          %% "scalatest"          % "3.2.15",
    "com.vladsch.flexmark"   % "flexmark-all"        % "0.64.8",
    "org.scalacheck"         %% "scalacheck"         % "1.17.0",
    "org.scalamock"          %% "scalamock"          % "5.2.0" ,
    "org.playframework"      %% "play-test"          % "3.0.2" ,
    "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1"
  ).map(_ % Test)

}
