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
import play.api.libs.functional.syntax._
import play.api.libs.json._
import utils.DateUtils._

case class SubmitReturnResponse(processingDate: DateTime,
                                formBundleNumber: Option[String],
                                paymentIndicator: Option[String],
                                chargeRefNumber: Option[String])

object SubmitReturnResponse {
  implicit val reads: Reads[SubmitReturnResponse] = (
    (JsPath \ "processingDate").read[DateTime](dateTimeFormat) or
      (JsPath \ "processingDate").read[DateTime](defaultDateTimeFormat) and
      (JsPath \ "formBundleNumber").readNullable[String] and
      (JsPath \ "paymentIndicator").readNullable[String] and
      (JsPath \ "chargeRefNumber").readNullable[String]
    ) (SubmitReturnResponse.apply _)

  implicit val dateFormats: Format[DateTime] = dateTimeFormat
  implicit val writes: Writes[SubmitReturnResponse] = Json.writes[SubmitReturnResponse]
}