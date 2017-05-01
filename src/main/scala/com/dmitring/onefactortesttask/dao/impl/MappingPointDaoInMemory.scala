package com.dmitring.onefactortesttask.dao.impl

import com.dmitring.onefactortesttask.dao.MappingPointDao
import com.dmitring.onefactortesttask.dao.MappingPointDao.MappingPointNotFound
import com.dmitring.onefactortesttask.model.MappingPoint
import com.dmitring.onefactortesttask.model.MappingPointCoordinates

import scala.concurrent.Future

class MappingPointDaoInMemory(
  mappingPoints: Seq[MappingPoint]
) extends MappingPointDao {

  import MappingPointDaoInMemory._
  import scala.concurrent.ExecutionContext.Implicits.global

  private val mappingPointsMap: scala.collection.Map[MappingPointCoordinates, MappingPointValue] =
    mappingPoints
      .map { mappingPoint =>
        (
          mappingPoint.coordinates,
          MappingPointValue(mappingPoint, createUserLabelIdsSet())
        )
      }
      .toMap

  override def findMappingPoint(coordinates: MappingPointCoordinates): Future[Option[MappingPoint]] = Future {
    mappingPointsMap
      .get(coordinates)
      .map(_.mappingPoint)
  }

  override def getMappingPoint(coordinates: MappingPointCoordinates): Future[MappingPoint] = Future {
    mappingPointsMap
      .getOrElse(coordinates, throw MappingPointNotFound(coordinates))
      .mappingPoint
  }

  override def addUserLabelToPoint(
    coordinates: MappingPointCoordinates,
    userLabelId: Int
  ): Future[Unit] = Future {
    mappingPointsMap
      .get(coordinates)
      .fold[Unit] {
        throw MappingPointNotFound(coordinates)
      } {
        _.userLabelIds += userLabelId
      }
  }

  override def deleteUserLabelFromPoint(
    coordinates: MappingPointCoordinates,
    userLabelId: Int
  ): Future[Unit] = Future {
    mappingPointsMap
      .get(coordinates)
      .fold[Unit] {
        throw MappingPointNotFound(coordinates)
      } {
        _.userLabelIds -= userLabelId
      }
  }

  override def moveUserLabel(
    oldCoordinates: MappingPointCoordinates,
    newCoordinates: MappingPointCoordinates,
    userId: Int
  ): Future[Unit] = Future {
    val oldPoint = mappingPointsMap.getOrElse(
      oldCoordinates,
      throw MappingPointNotFound(oldCoordinates)
    )
    val newPoint = mappingPointsMap.getOrElse(
      newCoordinates,
      throw MappingPointNotFound(newCoordinates)
    )

    oldPoint.userLabelIds -= userId
    newPoint.userLabelIds += userId
  }

  override def getUserLabelIds(
    coordinates: MappingPointCoordinates
  ): Future[Iterable[Int]] = Future {
    mappingPointsMap
      .get(coordinates)
      .fold {
        throw MappingPointNotFound(coordinates)
      } {
        _.userLabelIds
      }
  }
}

object MappingPointDaoInMemory {

  case class MappingPointValue(
    mappingPoint: MappingPoint,
    userLabelIds: scala.collection.mutable.Set[Int] // it must a thread-safe set
  )

  def createUserLabelIdsSet(): scala.collection.mutable.Set[Int] = {
    import scala.collection.JavaConverters._

    java.util.Collections.newSetFromMap(
      new java.util.concurrent.ConcurrentHashMap[Int, java.lang.Boolean]
    ).asScala
  }
}
