package com.dmitring.onefactortesttask.service.impl

import com.dmitring.onefactortesttask.model.MappingPointCoordinates
import com.dmitring.onefactortesttask.service.GeoPointToMappingPointService

class GeoPointToMappingPointServiceImpl extends GeoPointToMappingPointService {
  override def getMappingPointCoordinates(
    longitude: Double,
    latitude: Double
  ): MappingPointCoordinates = MappingPointCoordinates(
    longitude.toInt,
    latitude.toInt
  )
}
