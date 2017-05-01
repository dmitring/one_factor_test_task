package com.dmitring.onefactortesttask.service

import com.dmitring.onefactortesttask.model.MappingPointCoordinates

trait GeoPointToMappingPointService {
  def getMappingPointCoordinates(longitude: Double, latitude: Double): MappingPointCoordinates
}

