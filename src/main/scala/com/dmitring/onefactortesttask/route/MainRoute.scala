package com.dmitring.onefactortesttask.route

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import com.dmitring.onefactortesttask.dao.MappingPointDao.MappingPointNotFound
import com.dmitring.onefactortesttask.dao.UserGeoLabelDao.UserLabelNotFoundById
import com.dmitring.onefactortesttask.model.UserGeoLabel
import com.dmitring.onefactortesttask.service.GeoPointStatisticsService
import com.dmitring.onefactortesttask.service.GeoPointStatisticsService.MappingPointStatistics
import com.dmitring.onefactortesttask.service.UserLabelNearnessService
import com.dmitring.onefactortesttask.service.UserLabelService
import com.typesafe.scalalogging.StrictLogging
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.DefaultFormats
import org.json4s.native

import scala.util.Failure
import scala.util.Success

class MainRoute(
  userLabelService: UserLabelService,
  userLabelNearnessService: UserLabelNearnessService,
  geoPointStatisticsService: GeoPointStatisticsService
) extends Json4sSupport with StrictLogging {
  import akka.http.scaladsl.server.Directives._
  import MainRoute._
  import UserLabelUpsertRequest._

  implicit val serialization = native.Serialization
  implicit val formats = DefaultFormats

  def route: Route = userLabelRoutes ~ userLabelNearnessRoute ~ geoPointStatisticsRoute

  private def userLabelRoutes: Route = pathPrefix("user-label") {
    upsertUserLabelRoute ~ deleteUserLabelRoute
  }

  private def upsertUserLabelRoute: Route = path("upsert") {
    (post & entity(as[UserLabelUpsertRequest])) { request =>
      val userLabel = toDomain(request)
      onComplete(userLabelService.upsertUserLabel(userLabel)) {
        case Success(_) => complete(StatusCodes.OK)
        case Failure(exception) =>
          logger.error(s"While upserting a userLabel by $request, got en exception", exception)
          complete(StatusCodes.InternalServerError, exception.getMessage)
      }
    }
  }

  private def deleteUserLabelRoute: Route = path("delete") {
    (post & entity(as[UserLabelDeleteDto])) { request =>
      val userLabelId = request.userLabelId
      onComplete(userLabelService.deleteUserLabel(userLabelId)) {
        case Success(_) => complete(StatusCodes.OK)
        case Failure(exception) => exception match {
          case UserLabelNotFoundById(_) =>
            logger.warn(s"While deleting a userLabelId by $request, the user label has not been found", exception)
            complete(StatusCodes.NotFound, exception.getMessage)
          case _ =>
            logger.error(s"While deleting a userLabelId by $request, got en exception", exception)
            complete(StatusCodes.InternalServerError, exception.getMessage)
        }
      }
    }
  }

  private def userLabelNearnessRoute: Route = path("user-label-nearness") {
    (post & entity(as[UserLabelNearnessRequest])) { request =>
      val userLabelId = request.userLabelId
      onComplete(userLabelNearnessService.isUserLabelNearMappingPoint(userLabelId)) {
        case Success(isNear) => complete(UserLabelNearnessResponse(isNear))
        case Failure(exception) => exception match {
          case UserLabelNotFoundById(_) =>
            logger.warn(s"While determining nearness by $request, the user label has not been found", exception)
            complete(StatusCodes.NotFound, exception.getMessage)
          case _ =>
            logger.error(s"While determining nearness by $request, got en exception", exception)
            complete(StatusCodes.InternalServerError, exception.getMessage)
        }
      }
    }
  }

  private def geoPointStatisticsRoute: Route = path("geo-point-statistics") {
    (post & entity(as[GeoPointStatisticsRequest])) { request =>
      val longitude = request.longitude
      val latitude = request.latitude
      onComplete(geoPointStatisticsService.nearMappingPointStatistics(longitude, latitude)) {
        case Success(MappingPointStatistics(userLabelIds)) =>
          complete(GeoPointStatisticsResponse(userLabelIds.size, userLabelIds))
        case Failure(exception) => exception match {
          case MappingPointNotFound(_) =>
            logger.warn(s"While getting point statistics by $request, mapping point of user has been not found", exception)
            complete(StatusCodes.NotFound, exception.getMessage)
          case _ =>
            logger.error(s"While getting point statistics by $request, got en exception", exception)
            complete(StatusCodes.InternalServerError, exception.getMessage)
        }
      }
    }
  }
}

object MainRoute {
  case class UserLabelUpsertRequest(
    userLabelId: Int,
    longitude: Double,
    latitude: Double
  )
  object UserLabelUpsertRequest {
    def toDomain(userLabelDto: UserLabelUpsertRequest): UserGeoLabel = UserGeoLabel(
      userLabelDto.userLabelId,
      userLabelDto.longitude,
      userLabelDto.latitude
    )
  }

  case class UserLabelDeleteDto(userLabelId: Int)

  case class UserLabelNearnessRequest(userLabelId: Int)
  case class UserLabelNearnessResponse(isNearMappingPoint: Boolean)

  case class GeoPointStatisticsRequest(longitude: Double, latitude: Double)
  case class GeoPointStatisticsResponse(userLabelCount: Int, userLabelIds: Iterable[Int])
}
