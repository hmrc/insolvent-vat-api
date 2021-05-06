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

package v1.controllers

import cats.data.EitherT
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContentAsJson, ControllerComponents}
import play.mvc.Http.MimeTypes
import utils._
import v1.controllers.requestParsers.SubmitReturnRequestParser
import v1.models.errors._
import v1.models.request.SubmitReturnRawData
import v1.services._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubmitReturnController @Inject()(val authService: EnrolmentsAuthService,
                                       requestParser: SubmitReturnRequestParser,
                                       service: SubmitReturnService,
                                       idGenerator: IdGenerator,
                                       cc: ControllerComponents)
                                      (implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext = EndpointLogContext(
    controllerName = "SubmitReturnController",
    endpointName = "submitVatReturn"
  )

  def submitReturn(vrn: String): Action[JsValue] =
    authorisedAction(vrn).async(parse.json) { implicit request =>

      implicit val correlationId: String = idGenerator.generateCorrelationId
      logger.info(message = s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
        s"Submitting Vat Return for VRN : $vrn with correlationId : $correlationId")

      val rawData: SubmitReturnRawData = SubmitReturnRawData(
        vrn,
        AnyContentAsJson(request.body)
      )

      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](requestParser.parseRequest(rawData))
          serviceResponse <- EitherT(service.submitReturn(parsedRequest))
        } yield {

          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

          Created
            .withApiHeaders(serviceResponse.correlationId)
            .as(MimeTypes.JSON)
        }

    result.leftMap { errorWrapper =>
      val resCorrelationId = errorWrapper.correlationId
      val result = errorResult(errorWrapper).withApiHeaders(resCorrelationId)
      logger.warn(
        s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
          s"Error response received with CorrelationId: $resCorrelationId")

      result
    }.merge
  }

  private def errorResult(errorWrapper: ErrorWrapper) = {
    (errorWrapper.error: @unchecked) match {
      case BadRequestError | VrnFormatError| PeriodKeyFormatError |
           MtdErrorWithCustomMessage(RuleIncorrectOrEmptyBodyError.code) |
           MtdErrorWithCustomMessage(ValueFormatError.code)|
           UniqueIDFormatError | ReceivedAtFormatError => BadRequest(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
    }
  }
}