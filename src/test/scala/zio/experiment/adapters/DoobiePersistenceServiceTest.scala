package zio.experiment.adapters

import zio.blocking.Blocking
import zio.experiment.adapters.DoobiePersistenceService.{createUserTable, dropUserTable}
import zio.experiment.configuration.Configuration
import zio.experiment.domain.model.User.User
import zio.experiment.domain.model.UserNotFound
import zio.experiment.domain.port.UserPersistence
import zio.test.Assertion._
import zio.test.TestAspect.sequential
import zio.test._
import zio.test.environment.TestEnvironment
import zio.{Cause, RIO}

object DoobiePersistenceServiceTest extends DefaultRunnableSpec {

  val user: User = User.build(14, "usr").getOrElse(throw new Exception("this is a test and should have succeeded"))

  def spec: ZSpec[TestEnvironment, Failure] =
    suite("DoobiePersistenceService unit test")(
      testM("DELETE and DROP should work") {
        for {
          _        <- dropUserTable
          _        <- createUserTable
          _        <- dropUserTable
          _        <- createUserTable
        } yield assert(true)(isTrue)
      },
      testM("GET should return a UserNotFound if the element does not exist") {
        for {
          _        <- dropUserTable
          _        <- createUserTable
          notFound <- RIO.accessM[UserPersistence](_.get.get(100).either)
        } yield assert(notFound.swap.getOrElse(anything))(isSubtype[UserNotFound](anything))
      },
      testM("GET should return the user if it exists") {
        for {
          _      <- dropUserTable
          _      <- createUserTable
          stored <- RIO.accessM[UserPersistence](_.get.create(user)).either
          found  <- RIO.accessM[UserPersistence](_.get.get(user.id.value)).either
        } yield assert(stored)(isRight(equalTo(user))) && assert(found)(isRight(equalTo(user)))
      }
    )
      .provideSomeLayer[TestEnvironment](
      (Configuration.live >+> Blocking.live >+> DoobiePersistenceService.transactorLive >+> DoobiePersistenceService.live)
        .mapError(_ => TestFailure.Runtime(Cause.die(new Exception("die"))))
    )@@ sequential

  //TODO Add tests for max. string length in the "name" field.
  //TODO add tests for max. and min. size
}
