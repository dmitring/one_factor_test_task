package com.dmitring.onefactortesttask.util

object GeoDistance {
    // Approx Earth radius in metres according to https://en.wikipedia.org/wiki/Earth_radius#Mean_radius
    private val EARTH_RADIUS = 6371008.8

    def distanceMetres(longitude1: Double, latitude1: Double, longitude2: Double, latitude2: Double): Double = {
      val lon1Rad = Math.toRadians(longitude1)
      val lat1Rad = Math.toRadians(latitude1)
      val lon2Rad = Math.toRadians(longitude2)
      val lat2Rad = Math.toRadians(latitude2)

      val dLong = lon2Rad - lon1Rad
      val dLat = lat2Rad - lat1Rad

      val a = haversin(dLat) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * haversin(dLong)
      val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
      EARTH_RADIUS * c
    }

    private def haversin(arg: Double): Double = Math.pow(Math.sin(arg / 2), 2)
}
