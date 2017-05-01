package com.dmitring.onefactortesttask.dao.impl

import java.io.FileInputStream
import java.io.InputStream

import com.dmitring.onefactortesttask.config.DaoConfig
import com.dmitring.onefactortesttask.dao.MappingPointDao
import com.dmitring.onefactortesttask.dao.UserGeoLabelDao
import com.dmitring.onefactortesttask.model.MappingPoint
import com.dmitring.onefactortesttask.model.MappingPointCoordinates
import com.dmitring.onefactortesttask.model.UserGeoLabel
import com.dmitring.onefactortesttask.service.GeoPointToMappingPointService

import scala.concurrent.Future

object DaoInMemoryFromFileFactory {
  import resource._
  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val codec = scala.io.Codec.UTF8

  def loadAndCreate(
    daoConfig: DaoConfig,
    geoPointToMappingPointService: GeoPointToMappingPointService
  ): Future[(MappingPointDao, UserGeoLabelDao)] = {
    try {
      loadAndCreate(
        mappingInputStream = provideMappingsInputStreams(daoConfig),
        userLabelInputStream = provideUserLabelsInputStreams(daoConfig),
        geoPointToMappingPointService
      )
    } catch {
      case exception: Throwable => Future.failed(exception)
    }
  }

  def loadAndCreate(
    mappingInputStream: InputStream,
    userLabelInputStream: InputStream,
    geoPointToMappingPointService: GeoPointToMappingPointService
  ): Future[(MappingPointDao, UserGeoLabelDao)] = {
    val mappingPoints = loadEntities(mappingInputStream, readMappingPoint)
    val userLabels = loadEntities(userLabelInputStream, readUserLabel)

    val mappingPointsDao = new MappingPointDaoInMemory(mappingPoints)
    val userGeoLabelDao = new UserGeoLabelDaoInMemory()
    for {
      _ <- Future.traverse(userLabels)(userGeoLabelDao.upsert)
      _ <- Future.traverse(userLabels) { userLabel =>
        val mappingPointCoordinates = geoPointToMappingPointService.getMappingPointCoordinates(
          userLabel.longitude,
          userLabel.latitude
        )
        mappingPointsDao.addUserLabelToPoint(mappingPointCoordinates, userLabel.userLabelId)
      }
    } yield (mappingPointsDao, userGeoLabelDao)
  }

  private def provideMappingsInputStreams(daoConfig: DaoConfig) = {
    new FileInputStream(daoConfig.mappingFilePath)
  }

  private def provideUserLabelsInputStreams(daoConfig: DaoConfig) = {
    new FileInputStream(daoConfig.userLabelsFilePath)
  }

  private def loadEntities[T](inputStream: InputStream, reader: String => T): Seq[T] = {
    managed(scala.io.Source.fromInputStream(inputStream)) acquireAndGet { input =>
      input.getLines.map(reader).toVector
    }
  }

  private def readMappingPoint(source: String) = {
    val values = source.split(' ')
    require(values.length == 3)
    val longitude = values(0).toInt
    val latitude = values(1).toInt
    val distanceError = values(2).toDouble
    MappingPoint(MappingPointCoordinates(longitude, latitude), distanceError)
  }

  private def readUserLabel(source: String) = {
    val values = source.split(' ')
    require(values.length == 3)
    val userId = values(0).toInt
    val longitude = values(1).toDouble
    val latitude = values(2).toDouble
    UserGeoLabel(userId, longitude, latitude)
  }
}
