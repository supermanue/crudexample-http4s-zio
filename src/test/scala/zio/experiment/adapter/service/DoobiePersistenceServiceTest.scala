package zio.experiment.adapter.service

import zio.blocking.Blocking
import zio.experiment.adapter.DBTransactor
import zio.experiment.adapter.service.DoobiePersistenceService.{createUserTable, dropUserTable}
import zio.experiment.configuration.Configuration
import zio.experiment.domain.model.User.User
import zio.experiment.domain.model.UserNotFound
import zio.experiment.domain.port.UserRepository
import zio.test.Assertion._
import zio.test.TestAspect.sequential
import zio.test.environment.TestEnvironment
import zio.test.{DefaultRunnableSpec, TestFailure, ZSpec, assert}
import zio.{Cause, ZIO}

object DoobiePersistenceServiceTest extends DefaultRunnableSpec {

  val user: User = User.build(14, "usr").getOrElse(throw new Exception("this is a test and should have succeeded"))

  def beforeEach: ZIO[DBTransactor, Throwable, Unit] = dropUserTable.flatMap(_ => createUserTable)

  def spec: ZSpec[TestEnvironment, Failure] =
    suite("DoobiePersistenceService unit test")(
      testM("DELETE and DROP should work") {
        for {
          _ <- beforeEach
          _ <- dropUserTable
          _ <- createUserTable
        } yield assert(true)(isTrue)
      },
      testM("GET should return a UserNotFound if the element does not exist") {
        for {
          _        <- beforeEach
          notFound <- UserRepository.get(100).either
        } yield assert(notFound.swap.getOrElse(anything))(isSubtype[UserNotFound](anything))
      },
      testM("GET should return the user if it exists") {
        for {
          _      <- beforeEach
          stored <- UserRepository.create(user).either
          found  <- UserRepository.get(user.id.value).either
        } yield assert(stored)(isRight(equalTo(user))) && assert(found)(isRight(equalTo(user)))
      }
    ).provideSomeLayer[TestEnvironment](
      (Configuration.live >+> Blocking.live >+> DoobiePersistenceService.transactorLive >+> DoobiePersistenceService.live)
        .mapError(_ => TestFailure.Runtime(Cause.die(new Exception("die"))))
    ) @@ sequential

  //TODO Add tests for max. string length in the "name" field.
  //TODO add tests for max. and min. size
}
