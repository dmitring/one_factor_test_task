package com.dmitring.onefactortesttask.service.impl

import com.dmitring.onefactortesttask.dao.MappingPointDao
import com.dmitring.onefactortesttask.service.GeoPointStatisticsService
import com.dmitring.onefactortesttask.service.GeoPointStatisticsService.MappingPointStatistics
import com.dmitring.onefactortesttask.service.GeoPointToMappingPointService

import scala.concurrent.Future

class GeoPointStatisticsServiceImpl(
  mappingPointDao: MappingPointDao,
  geoPointToMappingPointService: GeoPointToMappingPointService
) extends GeoPointStatisticsService {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def nearMappingPointStatistics(
    longitude: Double,
    latitude: Double
  ): Future[MappingPointStatistics] = {
    val mappingPointCoordinates = geoPointToMappingPointService.getMappingPointCoordinates(longitude, latitude)
    mappingPointDao
      .getUserLabelIds(mappingPointCoordinates)
      .map(MappingPointStatistics)
  }
}
