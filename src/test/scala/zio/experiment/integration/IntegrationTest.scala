package zio.experiment.integration

import cats.effect._
import org.http4s.client._
import org.http4s.implicits.http4sLiteralsSyntax
import org.http4s.{ Method, Request, Uri }
import zio.experiment.Main
import zio.experiment.Main.appEnvironment
import zio.experiment.domain.model.DomainFixtures
import zio.test.Assertion.isTrue
import zio.test._
import zio.test.environment.TestEnvironment
import zio.{ ZEnv, ZIO }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

object IntegrationTest extends DefaultRunnableSpec with DomainFixtures {
  implicit val cs: ContextShift[IO] = IO.contextShift(global)
  implicit val timer: Timer[IO]     = IO.timer(global)
  val blocker: Blocker              = Blocker.liftExecutionContext(global)
  val httpClient: Client[IO]        = JavaNetClientBuilder[IO](blocker).create

  def startServer(): Unit = {
    val thread = new Thread {
      override def run(): Unit = {
        println("Starting server async in another thread")
        val program = Main.program
          .provideSomeLayer[ZEnv](appEnvironment)
        zio.Runtime.default.unsafeRun(program)
      }
    }
    thread.start()

    while (
      Try(httpClient.statusFromString("http://localhost:8083/healthcheck").unsafeRunSync().code).getOrElse(-1) != 204
    ) {
      Thread.sleep(1000)
    }
  }

  def spec: ZSpec[TestEnvironment, Failure] =
    suite("API")(
      testM("store a user, retrieve it and then delete it (steel thread)") {
        check(userGen) {
          user =>
            val postRequest = Request[IO](
              method = Method.POST,
              uri = uri"http://localhost:8083/"
            ).withEntity(s"""{"id":${user.id}, "name": "${user.name}"}""")
            val postResult = httpClient.status(postRequest).unsafeRunSync()

            val readingResult =
              httpClient.expect[String](s"http://localhost:8083/${user.id.value.toString}").unsafeRunSync()

            val deleteRequest =
              Request[IO](
                method = Method.DELETE,
                uri = Uri.unsafeFromString(s"http://localhost:8083/${user.id.value.toString}")
              )
            val deletionResult = httpClient.status(deleteRequest).unsafeRunSync()

            assert(postResult.code == 201)(isTrue) &&
            assert(readingResult.contains(user.id.value.toString))(isTrue) &&
            assert(readingResult.contains(user.name.value))(isTrue) &&
            assert(deletionResult.code == 200)(isTrue)
        }
      },
      test("return an error with malformed input JSON") {
        val postRequest = Request[IO](
          method = Method.POST,
          uri = uri"http://localhost:8083/"
        ).withEntity("""{this is not a correct JSON}""")
        val result = httpClient.status(postRequest).unsafeRunSync()
        assert(result.code == 400)(isTrue)
      },
      test("return an error with JSON with incorrect data format") {
        val postRequest = Request[IO](
          method = Method.POST,
          uri = uri"http://localhost:8083/"
        ).withEntity("""{"id":"not an int", "name": "Manuel"}""")
        val result = httpClient.status(postRequest).unsafeRunSync()

        val postRequest2 = Request[IO](
          method = Method.POST,
          uri = uri"http://localhost:8083/"
        ).withEntity("""{"id":19, "name": false}""")
        val result2 = httpClient.status(postRequest2).unsafeRunSync()

        assert(result.code == 422)(isTrue) &&
        assert(result2.code == 422)(isTrue)
      },
      test("return an error when retrieving a non existing user") {
        val result = httpClient.statusFromString("http://localhost:8083/999").unsafeRunSync()
        assert(result.code == 404)(isTrue)
      },
      test("return an error when deleting a non existing user") {
        val deleteRequest = Request[IO](method = Method.DELETE, uri = uri"http://localhost:8083/999")

        val result = httpClient.status(deleteRequest).unsafeRunSync()
        assert(result.code == 404)(isTrue)
      }
    ) @@ TestAspect.beforeAll(ZIO.succeed(startServer()))
}
