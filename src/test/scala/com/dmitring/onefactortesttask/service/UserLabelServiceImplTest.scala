package com.dmitring.onefactortesttask.service

import com.dmitring.onefactortesttask.dao.MappingPointDao
import com.dmitring.onefactortesttask.dao.MappingPointDao.MappingPointNotFound
import com.dmitring.onefactortesttask.dao.UserGeoLabelDao
import com.dmitring.onefactortesttask.dao.UserGeoLabelDao.UserLabelNotFoundById
import com.dmitring.onefactortesttask.model.MappingPoint
import com.dmitring.onefactortesttask.model.MappingPointCoordinates
import com.dmitring.onefactortesttask.model.UserGeoLabel
import com.dmitring.onefactortesttask.service.impl.UserLabelServiceImpl
import org.scalamock.scalatest.MockFactory
import org.scalatest.Matchers
import org.scalatest.WordSpec
import org.scalatest.concurrent.Futures

import scala.concurrent.Future

class UserLabelServiceImplTest extends WordSpec with Matchers with MockFactory with Futures {
  import org.scalatest.concurrent.ScalaFutures._

  val userLabel = UserGeoLabel(100500, 30.0d, 40.0d)
  val mappingPoint = MappingPoint(MappingPointCoordinates(30, 40), 10000)
  val strangeUnknownException = new IllegalArgumentException("some unknown exception")

  "upsertUserLabel" should {
    "return success if corresponding mapping exists" in {
      val userLabelDao = mock[UserGeoLabelDao]
      val mappingPointDao = mock[MappingPointDao]
      val geoPointToMappingPointService = mock[GeoPointToMappingPointService]

      (userLabelDao.upsert _)
        .expects(userLabel)
        .returning(Future.successful(true))
        .once()

      (geoPointToMappingPointService.getMappingPointCoordinates _)
        .expects(userLabel.longitude, userLabel.latitude)
        .returning(mappingPoint.coordinates)
        .once()

      (mappingPointDao.addUserLabelToPoint _)
        .expects(mappingPoint.coordinates, userLabel.userLabelId)
        .returning(Future.successful(true))
        .once()

      val userLabelService = new UserLabelServiceImpl(userLabelDao, mappingPointDao, geoPointToMappingPointService)
      userLabelService.upsertUserLabel(userLabel).futureValue
    }

    "return success even if corresponding mapping DOES NOT exists" in {
      val userLabelDao = mock[UserGeoLabelDao]
      val mappingPointDao = mock[MappingPointDao]
      val geoPointToMappingPointService = mock[GeoPointToMappingPointService]

      (userLabelDao.upsert _)
        .expects(userLabel)
        .returning(Future.successful(true))
        .once()

      (geoPointToMappingPointService.getMappingPointCoordinates _)
        .expects(userLabel.longitude, userLabel.latitude)
        .returning(mappingPoint.coordinates)
        .once()

      (mappingPointDao.addUserLabelToPoint _)
        .expects(mappingPoint.coordinates, userLabel.userLabelId)
        .returning(Future.failed(MappingPointNotFound(mappingPoint.coordinates)))
        .once()

      val userLabelService = new UserLabelServiceImpl(userLabelDao, mappingPointDao, geoPointToMappingPointService)
      userLabelService.upsertUserLabel(userLabel).futureValue
    }

    "return failure only if userLabelDao returns failure" in {
      val userLabelDao = mock[UserGeoLabelDao]
      val mappingPointDao = mock[MappingPointDao]
      val geoPointToMappingPointService = mock[GeoPointToMappingPointService]

      (userLabelDao.upsert _)
        .expects(userLabel)
        .returning(Future.failed(strangeUnknownException))
        .once()

      val userLabelService = new UserLabelServiceImpl(userLabelDao, mappingPointDao, geoPointToMappingPointService)
      userLabelService.upsertUserLabel(userLabel).failed.futureValue shouldBe strangeUnknownException
    }
  }

  "deleteUserLabel" should {
    "return success if corresponding mapping exists" in {
      val userLabelDao = mock[UserGeoLabelDao]
      val mappingPointDao = mock[MappingPointDao]
      val geoPointToMappingPointService = mock[GeoPointToMappingPointService]

      (userLabelDao.getById _)
        .expects(userLabel.userLabelId)
        .returning(Future.successful(userLabel))
        .once()

      (geoPointToMappingPointService.getMappingPointCoordinates _)
        .expects(userLabel.longitude, userLabel.latitude)
        .returning(mappingPoint.coordinates)
        .once()

      (mappingPointDao.deleteUserLabelFromPoint _)
        .expects(mappingPoint.coordinates, userLabel.userLabelId)
        .returning(Future.successful(true))
        .once()

      (userLabelDao.deleteById _)
        .expects(userLabel.userLabelId)
        .returning(Future.successful(true))
        .once()

      val userLabelService = new UserLabelServiceImpl(userLabelDao, mappingPointDao, geoPointToMappingPointService)
      userLabelService.deleteUserLabel(userLabel.userLabelId).futureValue
    }

    "return success even if corresponding mapping DOES NOT exists" in {
      val userLabelDao = mock[UserGeoLabelDao]
      val mappingPointDao = mock[MappingPointDao]
      val geoPointToMappingPointService = mock[GeoPointToMappingPointService]

      (userLabelDao.getById _)
        .expects(userLabel.userLabelId)
        .returning(Future.successful(userLabel))
        .once()

      (geoPointToMappingPointService.getMappingPointCoordinates _)
        .expects(userLabel.longitude, userLabel.latitude)
        .returning(mappingPoint.coordinates)
        .once()

      (mappingPointDao.deleteUserLabelFromPoint _)
        .expects(mappingPoint.coordinates, userLabel.userLabelId)
        .returning(Future.failed(MappingPointNotFound(mappingPoint.coordinates)))
        .once()

      (userLabelDao.deleteById _)
        .expects(userLabel.userLabelId)
        .returning(Future.successful(true))
        .once()

      val userLabelService = new UserLabelServiceImpl(userLabelDao, mappingPointDao, geoPointToMappingPointService)
      userLabelService.deleteUserLabel(userLabel.userLabelId).futureValue
    }

    "return failure only if userLabelDao returns failure" in {
      val userLabelIdNotFound = UserLabelNotFoundById(userLabel.userLabelId)

      val userLabelDao = mock[UserGeoLabelDao]
      val mappingPointDao = mock[MappingPointDao]
      val geoPointToMappingPointService = mock[GeoPointToMappingPointService]

      (userLabelDao.getById _)
        .expects(userLabel.userLabelId)
        .returning(Future.failed(userLabelIdNotFound))
        .once()

      val userLabelService = new UserLabelServiceImpl(userLabelDao, mappingPointDao, geoPointToMappingPointService)
      userLabelService.deleteUserLabel(userLabel.userLabelId).failed.futureValue shouldBe userLabelIdNotFound
    }
  }
}
