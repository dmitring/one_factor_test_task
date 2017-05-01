package com.dmitring.onefactortesttask.service.impl

import com.dmitring.onefactortesttask.dao.MappingPointDao
import com.dmitring.onefactortesttask.dao.UserGeoLabelDao
import com.dmitring.onefactortesttask.model.UserGeoLabel
import com.dmitring.onefactortesttask.service.GeoPointToMappingPointService
import com.dmitring.onefactortesttask.service.UserLabelService
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.Future

class UserLabelServiceImpl(
  userLabelDao: UserGeoLabelDao,
  mappingPointDao: MappingPointDao,
  geoPointToMappingPointService: GeoPointToMappingPointService
) extends UserLabelService with StrictLogging {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def upsertUserLabel(userLabel: UserGeoLabel): Future[Unit] = for {
    _ <- userLabelDao.upsert(userLabel)
    userMappingPoint = geoPointToMappingPointService.getMappingPointCoordinates(
      userLabel.longitude, userLabel.latitude
    )
    addUserIdToMappingPoint <- mappingPointDao
      .addUserLabelToPoint(userMappingPoint, userLabel.userLabelId)
      // userLabel actually have been added despite mappingPoint issues
      .recover { case exception =>
        logger.warn(s"While adding userLabel couldn't add to mappingPoint index", exception)
      }
  } yield addUserIdToMappingPoint

  override def deleteUserLabel(userLabelId: Int): Future[Unit] = for {
    userLabel <- userLabelDao.getById(userLabelId)
    userMappingPoint = geoPointToMappingPointService.getMappingPointCoordinates(
      userLabel.longitude, userLabel.latitude
    )
    _ <- mappingPointDao
      .deleteUserLabelFromPoint(userMappingPoint, userLabelId)
      // we should try to delete userLabel despite mappingPoint issues
      .recover { case exception =>
        logger.warn(s"While deleting userLabel couldn't delete from mappingPoint index", exception)
      }
    deleteUserLabel <- userLabelDao.deleteById(userLabelId)
  } yield deleteUserLabel
}
