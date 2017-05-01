package com.dmitring.onefactortesttask.dao

import com.dmitring.onefactortesttask.model.UserGeoLabel

import scala.concurrent.Future

trait UserGeoLabelDao {
  def findById(userLabelId: Int): Future[Option[UserGeoLabel]]
  def getById(userLabelId: Int): Future[UserGeoLabel]
  def deleteById(userLabelId: Int): Future[Unit]
  def upsert(userGeoLabel: UserGeoLabel): Future[Unit]
}

object UserGeoLabelDao {
  case class UserLabelNotFoundById(
    userLabelId: Int
  ) extends Exception(s"UserLabel hasn't been found by id: $userLabelId")
}
