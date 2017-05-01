package com.dmitring.onefactortesttask.service

import com.dmitring.onefactortesttask.dao.MappingPointDao
import com.dmitring.onefactortesttask.dao.MappingPointDao.MappingPointNotFound
import com.dmitring.onefactortesttask.dao.UserGeoLabelDao
import com.dmitring.onefactortesttask.dao.UserGeoLabelDao.UserLabelNotFoundById
import com.dmitring.onefactortesttask.model.MappingPoint
import com.dmitring.onefactortesttask.model.MappingPointCoordinates
import com.dmitring.onefactortesttask.model.UserGeoLabel
import com.dmitring.onefactortesttask.service.impl.UserLabelNearnessServiceImpl
import org.scalamock.scalatest.MockFactory
import org.scalatest.Matchers
import org.scalatest.WordSpec
import org.scalatest.concurrent.Futures

import scala.concurrent.Future

class UserLabelNearnessServiceImplTest extends WordSpec with Matchers with MockFactory with Futures {
  import org.scalatest.concurrent.ScalaFutures._

  val nearUserLabel = UserGeoLabel(100500, 30.1d, 40.2d)  // ~ 23794 metres to mapping point
  val farUserLabel = UserGeoLabel(300600, 30.9d, 40.9d) // ~ 125670 metres to mapping point
  val mappingPoint = MappingPoint(MappingPointCoordinates(30, 40), 120000)

  "isUserLabelNearMappingPoint" should {
    "return true if near" in {
      val userLabelDao = mock[UserGeoLabelDao]
      val mappingPointDao = mock[MappingPointDao]
      val geoPointToMappingPointService = mock[GeoPointToMappingPointService]

      (userLabelDao.getById _)
        .expects(nearUserLabel.userLabelId)
        .returning(Future.successful(nearUserLabel))
        .once()

      (geoPointToMappingPointService.getMappingPointCoordinates _)
        .expects(nearUserLabel.longitude, nearUserLabel.latitude)
        .returning(mappingPoint.coordinates)
        .once()

      (mappingPointDao.getMappingPoint _)
        .expects(mappingPoint.coordinates)
        .returning(Future.successful(mappingPoint))
        .once()

      val userLabelNearnessService = new UserLabelNearnessServiceImpl(
        userLabelDao,
        mappingPointDao,
        geoPointToMappingPointService
      )

      userLabelNearnessService.isUserLabelNearMappingPoint(nearUserLabel.userLabelId).futureValue shouldEqual true
    }

    "return false if far" in {
      val userLabelDao = mock[UserGeoLabelDao]
      val mappingPointDao = mock[MappingPointDao]
      val geoPointToMappingPointService = mock[GeoPointToMappingPointService]

      (userLabelDao.getById _)
        .expects(farUserLabel.userLabelId)
        .returning(Future.successful(farUserLabel))
        .once()

      (geoPointToMappingPointService.getMappingPointCoordinates _)
        .expects(farUserLabel.longitude, farUserLabel.latitude)
        .returning(mappingPoint.coordinates)
        .once()

      (mappingPointDao.getMappingPoint _)
        .expects(mappingPoint.coordinates)
        .returning(Future.successful(mappingPoint))
        .once()

      val userLabelNearnessService = new UserLabelNearnessServiceImpl(
        userLabelDao,
        mappingPointDao,
        geoPointToMappingPointService
      )

      userLabelNearnessService.isUserLabelNearMappingPoint(farUserLabel.userLabelId).futureValue shouldEqual false
    }

    "throw exception if userId doesn't exist" in {
      val userLabelIdNotFound = UserLabelNotFoundById(nearUserLabel.userLabelId)

      val userLabelDao = mock[UserGeoLabelDao]
      val mappingPointDao = mock[MappingPointDao]
      val geoPointToMappingPointService = mock[GeoPointToMappingPointService]

      (userLabelDao.getById _)
        .expects(nearUserLabel.userLabelId)
        .returning(Future.failed(userLabelIdNotFound))
        .once()

      val userLabelNearnessService = new UserLabelNearnessServiceImpl(
        userLabelDao,
        mappingPointDao,
        geoPointToMappingPointService
      )

      userLabelNearnessService.isUserLabelNearMappingPoint(nearUserLabel.userLabelId)
        .failed.futureValue shouldEqual userLabelIdNotFound
    }

    "throw exception if mapping point doesn't exist" in {
      val mappingPointNotFound = MappingPointNotFound(mappingPoint.coordinates)

      val userLabelDao = mock[UserGeoLabelDao]
      val mappingPointDao = mock[MappingPointDao]
      val geoPointToMappingPointService = mock[GeoPointToMappingPointService]

      (userLabelDao.getById _)
        .expects(nearUserLabel.userLabelId)
        .returning(Future.successful(nearUserLabel))
        .once()

      (geoPointToMappingPointService.getMappingPointCoordinates _)
        .expects(nearUserLabel.longitude, nearUserLabel.latitude)
        .returning(mappingPoint.coordinates)
        .once()

      (mappingPointDao.getMappingPoint _)
        .expects(mappingPoint.coordinates)
        .returning(Future.failed(mappingPointNotFound))
        .once()

      val userLabelNearnessService = new UserLabelNearnessServiceImpl(
        userLabelDao,
        mappingPointDao,
        geoPointToMappingPointService
      )

      userLabelNearnessService.isUserLabelNearMappingPoint(nearUserLabel.userLabelId)
        .failed.futureValue shouldEqual mappingPointNotFound
    }
  }
}
