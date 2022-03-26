package zio.experiment.domain.model

import zio.test.Assertion._
import zio.test._
import zio.test.environment.TestEnvironment

import scala.util.Try

object FixturesTest extends DefaultRunnableSpec with DomainFixtures {
  def spec: ZSpec[TestEnvironment, Failure] =
    suite("Fixtures test")(
      testM("positiveIntGen generates  positive ints") {
        check(positiveIntGen) { positiveInt =>
          assert(positiveInt > 0)(isTrue)
        }
      },
      testM("nonemptyStringGen generates nonempty strings") {
        check(nonemptyStringGen) { nonemptyString =>
          assert(nonemptyString.nonEmpty)(isTrue)
        }
      },
      testM("userGen builds valid users") {
        check(userGen) { user =>
          assert(Try(user))(isSuccess)
        }
      }
    )
}
