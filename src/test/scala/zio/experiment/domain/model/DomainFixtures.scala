package zio.experiment.domain.model

import zio.experiment.domain.model.User.User
import zio.random.Random
import zio.test.Gen._
import zio.test.{ Gen, Sized }

trait DomainFixtures {

  def positiveIntGen: Gen[Random, Int]                  = anyInt.map(num => math.abs(num) + 1)
  def nonemptyStringGen: Gen[Random with Sized, String] = (alphaNumericString <*> alphaNumericChar).map(elems => elems._1 + elems._2)

  def userGen: Gen[Random with Sized, User] =
    (positiveIntGen <*> nonemptyStringGen).map(elems =>
      User.build(elems._1, elems._2).getOrElse(throw new Exception("Exception in test building customer"))
    )
}
