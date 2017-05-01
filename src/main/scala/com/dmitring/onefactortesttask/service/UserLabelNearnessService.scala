package com.dmitring.onefactortesttask.service

import scala.concurrent.Future

trait UserLabelNearnessService {
  def isUserLabelNearMappingPoint(userLabelId: Int): Future[Boolean]
}
