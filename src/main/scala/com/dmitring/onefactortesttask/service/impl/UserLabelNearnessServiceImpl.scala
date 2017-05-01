package com.dmitring.onefactortesttask.service.impl

import com.dmitring.onefactortesttask.dao.MappingPointDao
import com.dmitring.onefactortesttask.dao.UserGeoLabelDao
import com.dmitring.onefactortesttask.service.GeoPointToMappingPointService
import com.dmitring.onefactortesttask.service.UserLabelNearnessService
import com.dmitring.onefactortesttask.util.GeoDistance

import scala.concurrent.Future

class UserLabelNearnessServiceImpl(
  userGeoLabelDao: UserGeoLabelDao,
  mappingPointDao: MappingPointDao,
  geoPointToMappingPointService: GeoPointToMappingPointService
) extends UserLabelNearnessService {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def isUserLabelNearMappingPoint(userLabelId: Int): Future[Boolean] = for {
    userLabel <- userGeoLabelDao.getById(userLabelId)
    mappingPointCoordinates = geoPointToMappingPointService.getMappingPointCoordinates(
      userLabel.longitude, userLabel.latitude
    )
    mappingPoint <- mappingPointDao.getMappingPoint(mappingPointCoordinates)
    distanceMetres = GeoDistance.distanceMetres(
      userLabel.longitude, userLabel.latitude,
      mappingPoint.coordinates.longitude, mappingPoint.coordinates.latitude
    )
  } yield distanceMetres <= mappingPoint.distanceErrorMetres
}
