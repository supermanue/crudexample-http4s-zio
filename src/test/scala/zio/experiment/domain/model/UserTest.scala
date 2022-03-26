package zio.experiment.domain.model

import zio.experiment.domain.model.User.User
import zio.test.Assertion._
import zio.test._
import zio.test.environment.TestEnvironment

object UserTest extends DefaultRunnableSpec with DomainFixtures {
  def spec: ZSpec[TestEnvironment, Failure] =
    suite("UserTest")(
      testM("creating a user with valid input should succeed") {
        check(positiveIntGen, nonemptyStringGen) { (positiveInt, nonemptyString) =>
          val user = User.build(positiveInt, nonemptyString)
          assert(user)(isRight)
        }
      },
      testM("creating a user with id=0 should fail") {
        check(nonemptyStringGen) { nonemptyString =>
          val user = User.build(0, nonemptyString)
          assert(user.left.getOrElse(false).isInstanceOf[RefinedTypeError])(isTrue)
        }
      },
      testM("creating a user with negative id should fail") {
        check(nonemptyStringGen) { nonemptyString =>
          val user = User.build(-1, nonemptyString)
          assert(user.left.getOrElse(false).isInstanceOf[RefinedTypeError])(isTrue)
        }
      },
      testM("creating a user with an empty name should fail") {
        check(positiveIntGen) { positiveInt =>
          val user = User.build(positiveInt, "")
          assert(user.left.getOrElse(false).isInstanceOf[RefinedTypeError])(isTrue)
        }
      }
    )
}
