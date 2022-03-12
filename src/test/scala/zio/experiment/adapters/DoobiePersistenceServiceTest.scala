package zio.experiment.adapters

import zio.Cause
import zio.blocking.Blocking
import zio.experiment.configuration.Configuration
import zio.experiment.domain.model.{User, UserNotFound}
import zio.experiment.domain.service.UserService._
import zio.test.Assertion._
import zio.test._
import zio.test.environment.TestEnvironment

object DoobiePersistenceServiceTest extends DefaultRunnableSpec {

  def spec =
    suite("DoobiePersistenceService unit test")(testM("Persistence Live") {
      for {
        _ <- DoobiePersistenceService.createUserTable
        notFound <- getUser(
          100
        ).either //TODO these calls should be to persistenceService.get and so. Here we are mixing domain and adapters
        created <- createUser(User(14, "usr")).either
        deleted <- deleteUser(14).either
      } yield assert(notFound.swap.getOrElse(anything))(isSubtype[UserNotFound](anything)) &&
        assert(created)(isRight(equalTo(User(14, "usr")))) &&
        assert(deleted)(isRight(isTrue))
    }).provideSomeLayer[TestEnvironment](
      (Configuration.live >+> Blocking.live >+> DoobiePersistenceService.transactorLive >+> DoobiePersistenceService.live)
        .mapError(_ => TestFailure.Runtime(Cause.die(new Exception("die"))))
    )

  //TODO Add tests for max. string length in the "name" field.
  //TODO add tests for max. and min. size
  //TODO split in different testM tests, not all together in a big one
}
