package com.dmitring.onefactortesttask

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.dmitring.onefactortesttask.config.DaoConfig
import com.dmitring.onefactortesttask.config.HttpConfig
import com.dmitring.onefactortesttask.dao.impl.DaoInMemoryFromFileFactory
import com.dmitring.onefactortesttask.route.MainRoute
import com.dmitring.onefactortesttask.service.impl.GeoPointStatisticsServiceImpl
import com.dmitring.onefactortesttask.service.impl.GeoPointToMappingPointServiceImpl
import com.dmitring.onefactortesttask.service.impl.UserLabelNearnessServiceImpl
import com.dmitring.onefactortesttask.service.impl.UserLabelServiceImpl
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging

import scala.util.Failure
import scala.util.Success

object Main extends App with StrictLogging {
  import scala.concurrent.ExecutionContext.Implicits.global

    val configs = ConfigFactory.load()
    val httpConfig = HttpConfig(configs.getConfig("http"))
    val daoConfig = DaoConfig(configs.getConfig("dao"))

    implicit val actorSystem = ActorSystem()
    implicit val materializer = ActorMaterializer()

    val geoPointToMappingPointService = new GeoPointToMappingPointServiceImpl()
    val serverStartFuture = for {
      (mappingPointDao, userLabelDao) <- DaoInMemoryFromFileFactory.loadAndCreate(
        daoConfig, geoPointToMappingPointService)
      userLabelService = new UserLabelServiceImpl(
        userLabelDao,
        mappingPointDao,
        geoPointToMappingPointService
      )
      userLabelNearnessService = new UserLabelNearnessServiceImpl(
        userLabelDao,
        mappingPointDao,
        geoPointToMappingPointService
      )
      geoPointStatisticsService = new GeoPointStatisticsServiceImpl(
        mappingPointDao,
        geoPointToMappingPointService
      )
      mainRoute = new MainRoute(
        userLabelService,
        userLabelNearnessService,
        geoPointStatisticsService
      )

      serverBinding <- Http().bindAndHandle(mainRoute.route, httpConfig.host, httpConfig.port)
    } yield serverBinding

    serverStartFuture.onComplete {
      case Success(_) =>
        logger.info(s"Application has successfully started, It's listening on $httpConfig")
      case Failure(exception) =>
        logger.error("An error has occurred while application start", exception)
        actorSystem.terminate().onComplete { _ => sys.exit(1) }
    }
}
