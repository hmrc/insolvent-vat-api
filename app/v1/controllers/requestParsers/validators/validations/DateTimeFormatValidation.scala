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

package v1.controllers.requestParsers.validators.validations

import java.time.LocalDateTime
import java.time.format.{DateTimeFormatter, ResolverStyle}

import v1.models.errors.{MtdError, ReceivedAtFormatError}

import scala.util.{Failure, Success, Try}


object DateTimeFormatValidation {

  val dateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss'Z'").withResolverStyle(ResolverStyle.STRICT)

  def validate(dateTime: String): List[MtdError] = Try {
    LocalDateTime.parse(dateTime, dateTimeFormat)
  } match {
    case Success(_) => NoValidationErrors
    case Failure(_) => List(ReceivedAtFormatError)
  }
}
