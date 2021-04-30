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
import config.AppConfig

import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContentAsJson, ControllerComponents}
import play.mvc.Http.MimeTypes
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils._
import v1.controllers.requestParsers.AmendSampleRequestParser
import v1.hateoas.AmendHateoasBodies
import v1.models.audit.{AuditEvent, AuditDetail, AuditResponse}
import v1.models.errors._
import v1.models.request._
import v1.services._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubmitReturnController @Inject()(val authService: EnrolmentsAuthService,
                                       appConfig: AppConfig,
                                       requestParser: AmendSampleRequestParser,
                                       service: AmendSampleService,
                                       auditService: AuditService,
                                       idGenerator: IdGenerator,
                                       dateTime: CurrentDateTime,
                                      cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging with AmendHateoasBodies {

  implicit val endpointLogContext: EndpointLogContext = EndpointLogContext(
    controllerName = "SubmitReturnController",
    endpointName = "submitVatReturn"
  )

  def submitReturn(vrn: String): Action[JsValue] =
    authorisedAction(vrn).async(parse.json) { implicit request =>

      implicit val correlationId: String = idGenerator.getUid
      logger.info(message = s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
        s"Submitting Vat Return for VRN : $vrn with correlationId : $correlationId")

      val rawRequest: SubmitReturnRawData = SubmitReturnRawData(
        vrn,
        AnyContentAsJson(request.body)
      )

      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](requestParser.parseRequest(rawRequest))
          serviceResponse <- EitherT(service.amendSample(parsedRequest))
        } yield {

          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

          auditSubmission(
            AuditDetail(
              request.userDetails, Map("vrn" -> vrn), Some(request.body), serviceResponse.correlationId,
              AuditResponse(httpStatus = OK, response = Right(Some(amendSampleHateoasBody(appConfig, vrn))))
            )
          )

          Ok(amendSampleHateoasBody(appConfig, vrn))
          .withApiHeaders(serviceResponse.correlationId)
          .as(MimeTypes.JSON)
          }

    result.leftMap { errorWrapper =>
      val resCorrelationId = errorWrapper.correlationId
      val result = errorResult(errorWrapper).withApiHeaders(resCorrelationId)
      logger.warn(
        s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
          s"Error response received with CorrelationId: $resCorrelationId")

      auditSubmission(
        AuditDetail(
          request.userDetails, Map("vrn" -> vrn), Some(request.body),
          resCorrelationId, AuditResponse(httpStatus = result.header.status, response = Left(errorWrapper.auditErrors))        )
      )

      result
    }.merge
  }

  private def errorResult(errorWrapper: ErrorWrapper) = {
    (errorWrapper.error: @unchecked) match {
      case VrnFormatError | VrnFormatErrorDes | BadRequestError |
           PeriodKeyFormatError | PeriodKeyFormatErrorDes | BodyPeriodKeyFormatError |
           RuleIncorrectOrEmptyBodyError | FormatUniqueIDError => BadRequest(Json.toJson(errorWrapper))
      case TaxPeriodNotEnded | DuplicateVatSubmission | RuleInsolventTraderError => Forbidden(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
      case _: MtdError => BadRequest(Json.toJson(errorWrapper))
    }
  }

  private def auditSubmission(details: AuditDetail)
                             (implicit hc: HeaderCarrier,
                              ec: ExecutionContext): Future[AuditResult] = {
    val event = AuditEvent("SubmitReturn", "submit-return", details)
    auditService.auditEvent(event)
  }
}