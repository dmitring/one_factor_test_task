package com.dmitring.onefactortesttask.dao.impl

import com.dmitring.onefactortesttask.dao.UserGeoLabelDao
import com.dmitring.onefactortesttask.dao.UserGeoLabelDao.UserLabelNotFoundById
import com.dmitring.onefactortesttask.model.UserGeoLabel

import scala.concurrent.Future

class UserGeoLabelDaoInMemory extends UserGeoLabelDao {

  import scala.concurrent.ExecutionContext.Implicits.global

  private val usersMap: scala.collection.concurrent.Map[Int, UserGeoLabel] =
    scala.collection.concurrent.TrieMap()

  override def findById(userLabelId: Int): Future[Option[UserGeoLabel]] = Future {
    usersMap.get(userLabelId)
  }


  override def getById(userLabelId: Int): Future[UserGeoLabel] = Future {
    usersMap.getOrElse(userLabelId, throw UserLabelNotFoundById(userLabelId))
  }

  override def deleteById(userLabelId: Int): Future[Unit] = Future {
    usersMap -= userLabelId
  }

  override def upsert(userGeoLabel: UserGeoLabel): Future[Unit] = Future {
    usersMap += userGeoLabel.userLabelId -> userGeoLabel
  }
}
