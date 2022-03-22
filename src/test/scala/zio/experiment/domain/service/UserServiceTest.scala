package zio.experiment.domain.service

import zio.experiment.domain.model.User.User
import zio.experiment.domain.service.UserService.{createUser, deleteUser, getUser}
import zio.test.Assertion._
import zio.test.environment.TestEnvironment
import zio.test.{DefaultRunnableSpec, assert, assertM}

object UserServiceTest extends DefaultRunnableSpec {

  val user = User.build(14, "usr").getOrElse(new Exception("this is a test and should have succeeded"))

  def spec =
    suite("UserService unit test")(
      testM("get a non existing user should fail") {
        assertM(getUser(100).run)(fails(anything))
      },
      testM("create a user then get it ") {
        for {
          created <- createUser(14, "usr")
          user    <- getUser(14)
        } yield assert(created)(equalTo(user)) &&
          assert(user)(equalTo(user))
      },
      testM("delete user") {
        for {
          deleted  <- deleteUser(14).either
          notFound <- getUser(14).either
        } yield assert(deleted)(isRight(isTrue)) &&
          assert(notFound)(isLeft(anything))
      }
    ).provideSomeLayer[TestEnvironment](TestDB.layer)
}
