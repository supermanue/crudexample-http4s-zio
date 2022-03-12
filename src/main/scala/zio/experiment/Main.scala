package zio.experiment

import cats.effect.{ExitCode => CatsExitCode}
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console.putStrLn
import zio.experiment.adapters.{DBTransactor, DoobiePersistenceService}
import zio.experiment.configuration.Configuration
import zio.experiment.domain.port.UserPersistence
import zio.experiment.http.Api
import zio.interop.catz._

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object Main extends App {
  val ec: ExecutionContextExecutor = ExecutionContext.global
  type AppEnvironment = Configuration with Clock with DBTransactor with UserPersistence

  type AppTask[A] = RIO[AppEnvironment, A]

  val appEnvironment =
    Configuration.live >+> Blocking.live >+> DoobiePersistenceService.transactorLive >+> DoobiePersistenceService.live

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] = {
    val program: ZIO[AppEnvironment, Throwable, Unit] =
      for {
        _   <- DoobiePersistenceService.createUserTable
        api <- configuration.apiConfig
        httpApp = Router[AppTask](
          "/" -> Api(s"${api.endpoint}/").route
        ).orNotFound

        server <- ZIO.runtime[AppEnvironment].flatMap { implicit rts =>
          BlazeServerBuilder[AppTask](ec)
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
