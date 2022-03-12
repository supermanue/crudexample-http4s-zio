package zio.experiment.integration

import cats.effect.Blocker
import org.http4s.client.JavaNetClientBuilder
import zio._
import zio.clock.Clock
import zio.experiment.Main
import zio.interop.catz.{taskConcurrentInstance, zioContextShift}
import zio.test.Assertion._
import zio.test._
import zio.test.environment.TestClock

import java.net.Socket
import java.time.Duration.ofSeconds
import scala.concurrent.ExecutionContext

object MainTest extends DefaultRunnableSpec {
  val ec: ExecutionContext = ExecutionContext.global
  def httpServer =
    Main.run(List()).forkManaged.toLayer

  val clockDuration = ofSeconds(1)
  val blocker       = Blocker.liftExecutionContext(ec)

  //did the httpserver start listening on 8080?
  private def isLocalPortInUse(port: Int): ZIO[Clock, Throwable, Unit] = {
    IO.effect(new Socket("0.0.0.0", port).close()).retry(Schedule.linear(clockDuration) && Schedule.recurs(10))
  }

  override def spec: ZSpec[Environment, Failure] =
    suite("MainTest")(
      testM("Health check") {
        for {
          _        <- TestClock.adjust(clockDuration).fork
          _        <- isLocalPortInUse(8080)
          client   <- Task(JavaNetClientBuilder[Task](blocker).create)
          response <- client.expect[String]("http://localhost:8083/healthcheck")
        } yield assert(response) {
          equalTo("")
        }
      }
    ).provideCustomLayerShared(httpServer)
}
