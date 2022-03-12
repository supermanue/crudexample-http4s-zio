package com.bound.exercise

import cats.effect.{ExitCode => CatsExitCode}
import com.bound.exercise.http.Api
import com.bound.exercise.configuration.Configuration
import com.bound.exercise.persistence.{DBTransactor, UserPersistence, UserPersistenceService}
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console.putStrLn
import zio.interop.catz._

object Main extends App {

  type AppEnvironment = Configuration with Clock with DBTransactor with UserPersistence

  type AppTask[A] = RIO[AppEnvironment, A]

  val appEnvironment =
    Configuration.live >+> Blocking.live >+> UserPersistenceService.transactorLive >+> UserPersistenceService.live

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] = {
    val program: ZIO[AppEnvironment, Throwable, Unit] =
      for {
        _   <- UserPersistenceService.createUserTable
        api <- configuration.apiConfig
        httpApp = Router[AppTask](
          "/" -> Api(s"${api.endpoint}/").route
        ).orNotFound

        server <- ZIO.runtime[AppEnvironment].flatMap { implicit rts =>
          BlazeServerBuilder[AppTask]
            .bindHttp(api.port, api.endpoint)
            .withHttpApp(CORS(httpApp))
            .serve
            .compile[AppTask, AppTask, CatsExitCode]
            .drain
        }
      } yield server

    program
      .provideSomeLayer[ZEnv](appEnvironment)
      .tapError(err => putStrLn(s"Execution failed with: $err"))
      .exitCode
  }
}
