package com.dmitring.onefactortesttask.util

import org.scalatest.Matchers
import org.scalatest.WordSpec

class GeoDistanceTest extends WordSpec with Matchers {
  "GeoDistance.distanceMetres" should {
    "get a small error (less than 0.1%) on very close points" in {
      val lon1 = -137.1
      val lat1 = -28.7

      val lon2 = -137.2
      val lat2 = -28.6

      val etalonDifferenceMetres = 14784
      val givenDifferenceMetres = GeoDistance.distanceMetres(lon1, lat1, lon2, lat2)
      val differenceMetres = Math.abs(givenDifferenceMetres - etalonDifferenceMetres)

      differenceMetres should be < etalonDifferenceMetres * 0.001d
    }

    "get a small error (less than 0.1%) on close points" in {
      val lon1 = 30.0
      val lat1 = 40.0

      val lon2 = 31.0
      val lat2 = 41.0

      val etalonDifferenceMetres = 139595
      val givenDifferenceMetres = GeoDistance.distanceMetres(lon1, lat1, lon2, lat2)
      val differenceMetres = Math.abs(givenDifferenceMetres - etalonDifferenceMetres)

      differenceMetres should be < etalonDifferenceMetres * 0.001d
    }

    "get a small error (less than 0.1%) on far points" in {
      val lon1 = 10.0
      val lat1 = -20.0

      val lon2 = 25.0
      val lat2 = 0.0

      val etalonDifferenceMetres = 2757356
      val givenDifferenceMetres = GeoDistance.distanceMetres(lon1, lat1, lon2, lat2)
      val differenceMetres = Math.abs(givenDifferenceMetres - etalonDifferenceMetres)

      differenceMetres should be < etalonDifferenceMetres * 0.001d
    }

    "get a small error (less than 0.1%) on  very far points" in {
      val lon1 = -170.0
      val lat1 = -80.0

      val lon2 = 164.0
      val lat2 = 76.0

      val etalonDifferenceMetres = 17402072
      val givenDifferenceMetres = GeoDistance.distanceMetres(lon1, lat1, lon2, lat2)
      val differenceMetres = Math.abs(givenDifferenceMetres - etalonDifferenceMetres)

      differenceMetres should be < etalonDifferenceMetres * 0.001d
    }
  }
}
