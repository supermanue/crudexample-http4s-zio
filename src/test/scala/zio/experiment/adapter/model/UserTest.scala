package zio.experiment.adapter.model

import zio.experiment.domain.model.{DomainFixtures, RefinedTypeError}
import zio.test.Assertion._
import zio.test._
import zio.test.environment.TestEnvironment

object UserTest extends DefaultRunnableSpec with DomainFixtures {
  def spec: ZSpec[TestEnvironment, Failure] =
    suite("UserTest")(
      testM("toDomainUser should convert correct users") {
        check(positiveIntGen, nonemptyStringGen) { (positiveInt, nonemptyString) =>
          val user       = User(positiveInt, nonemptyString)
          val domainUser = user.toDomainUser
          assert(domainUser)(isRight)
        }
      },
      testM("toDomainUser should return a AppError if the user cannot be created in the domain layer") {
        check(nonemptyStringGen) { nonemptyString =>
          val user       = User(0, nonemptyString)
          val domainUser = user.toDomainUser
          assert(domainUser.left.getOrElse(false).isInstanceOf[RefinedTypeError])(isTrue)
        }
      },
      testM("fromDomainUser should always return a valid User") {
        check(userGen) { userDomain =>
          val user = User.fromDomainUser(userDomain)
          assert(user.id == userDomain.id.value)(isTrue) &&
          assert(user.name == userDomain.name.value)(isTrue)
        }
      }
    )
}
