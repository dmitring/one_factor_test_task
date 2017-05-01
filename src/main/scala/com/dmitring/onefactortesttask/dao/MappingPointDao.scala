package com.dmitring.onefactortesttask.dao

import com.dmitring.onefactortesttask.model.MappingPoint
import com.dmitring.onefactortesttask.model.MappingPointCoordinates

import scala.concurrent.Future

trait MappingPointDao {
  def findMappingPoint(coordinates: MappingPointCoordinates): Future[Option[MappingPoint]]
  def getMappingPoint(coordinates: MappingPointCoordinates): Future[MappingPoint]

  def addUserLabelToPoint(coordinates: MappingPointCoordinates, userLabelId: Int): Future[Unit]
  def deleteUserLabelFromPoint(coordinates: MappingPointCoordinates, userId: Int): Future[Unit]
  def moveUserLabel(
    oldCoordinates: MappingPointCoordinates,
    newCoordinates: MappingPointCoordinates,
    userId: Int
  ): Future[Unit]
  def getUserLabelIds(coordinates: MappingPointCoordinates): Future[Iterable[Int]]
}

object MappingPointDao {
  case class MappingPointNotFound(
    coordinates: MappingPointCoordinates
  ) extends Exception(s"mapping with coordinates $coordinates hasn't been found")
}
