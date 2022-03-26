package zio.experiment.domain.service

import zio.experiment.domain.model.{DomainFixtures, UserNotFound}
import zio.experiment.domain.service.UserService.{createUser, deleteUser, getUser}
import zio.test.Assertion._
import zio.test._
import zio.test.environment.TestEnvironment

object UserServiceTest extends DefaultRunnableSpec with DomainFixtures {

  def spec: ZSpec[TestEnvironment, Failure] =
    suite("UserService unit test")(
      testM("get a non existing user should fail") {
        assertM(getUser(100).run)(fails(anything))
      },
      testM("create a user then get it should return the same user ") {
        checkM(userGen) { user =>
          for {
            created <- createUser(user.id.value, user.name.value)
            retrieved <- getUser(user.id.value)
          } yield assert(created)(equalTo(user)) &&
            assert(retrieved)(equalTo(user))
        }
      },
      testM("delete user should return true if it is deleted") {
        for {
          deleted <- deleteUser(14).either
          notFound <- getUser(14).either
        } yield assert(deleted)(isRight(isTrue)) &&
          assert(notFound.left.getOrElse(false).isInstanceOf[UserNotFound])(isTrue)
      }
    ).provideSomeLayer[TestEnvironment](TestDB.layer)
}
