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
import uk.gov.hmrc.DefaultBuildSettings.addTestReportOption
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion
import uk.gov.hmrc.DefaultBuildSettings


val appName = "insolvent-vat-api"

ThisBuild / scalaVersion := "2.13.12"
ThisBuild / majorVersion := 0

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    retrieveManaged := true,
    update / evictionWarningOptions := EvictionWarningOptions.default.withWarnScalaVersionEviction(warnScalaVersionEviction = false)
  )
  .settings(
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources"
  )
  .settings(CodeCoverageSettings.settings: _*)
  .settings(
    Runtime / unmanagedClasspath += baseDirectory.value / "resources"
  )
  .settings(
    resolvers += Resolver.jcenterRepo
  )
  .settings(PlayKeys.playDefaultPort := 9676)

dependencyUpdatesFilter -= moduleFilter(organization = "org.playframework")
dependencyUpdatesFilter -= moduleFilter(name = "scala-library")
dependencyUpdatesFilter -= moduleFilter(name = "flexmark-all")
dependencyUpdatesFilter -= moduleFilter(name = "scalatestplus-play")
dependencyUpdatesFilter -= moduleFilter(name = "scalatestplus-scalacheck")
dependencyUpdatesFilter -= moduleFilter(name = "scalamock")
dependencyUpdatesFilter -= moduleFilter(name = "bootstrap-backend-play-30")
dependencyUpdatesFailBuild := true


lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(DefaultBuildSettings.itSettings())
  .settings(libraryDependencies ++= AppDependencies.test,
    addTestReportOption(Test, "int-test-reports"))
  .settings(headerSettings(Test): _*)
  .settings(automateHeaderSettings(Test))
  .settings(javaOptions += "-Dlogger.resource=logback-test.xml")