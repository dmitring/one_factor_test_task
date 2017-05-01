package com.dmitring.onefactortesttask.service

import com.dmitring.onefactortesttask.model.UserGeoLabel

import scala.concurrent.Future

trait UserLabelService {
  def upsertUserLabel(userLabel: UserGeoLabel): Future[Unit]
  def deleteUserLabel(userLabelId: Int): Future[Unit]
}

