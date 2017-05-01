package com.dmitring.onefactortesttask.service

import com.dmitring.onefactortesttask.service.GeoPointStatisticsService.MappingPointStatistics

import scala.concurrent.Future

trait GeoPointStatisticsService {
  def nearMappingPointStatistics(longitude: Double, latitude: Double): Future[MappingPointStatistics]
}

object GeoPointStatisticsService {
  case class MappingPointStatistics(userLabelIds: Iterable[Int])
}
