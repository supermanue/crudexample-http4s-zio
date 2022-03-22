package zio.experiment.domain.model

import eu.timepit.refined.api.Refined
import eu.timepit.refined.boolean.{And, Not}
import eu.timepit.refined.char.Whitespace
import eu.timepit.refined.collection.{Forall, Size}
import eu.timepit.refined.numeric.{Interval, Positive}
import eu.timepit.refined.{W, refineV}

object User {
  sealed abstract case class User private (
      id: Id,
      name: Name
  )
  object User {
    def build(id: Int, name: String): Either[AppError, User] =
      for {
        refinedId   <- toRefinedId(id)
        refinedName <- toRefinedName(name)
      } yield new User(refinedId, refinedName) {}
  }
  /*
  Management of Refined Types for User
   */
  type Name = String Refined NameRestrictions
  type Id   = Int Refined IdRestrictions

  //string between 1 and 1000 characters, not all of them white
  type NameRestrictions = Size[Interval.Closed[W.`1`.T, W.`1000`.T]] And Not[Forall[Whitespace]]
  type IdRestrictions   = Positive

  private def toRefinedId(id: Int): Either[RefinedTypeError, Id] =
    refineV[IdRestrictions](id).left
      .map(_ => RefinedTypeError("must be a positive int", id.toString))

  private def toRefinedName(name: String): Either[RefinedTypeError, Name] =
    refineV[NameRestrictions](name).left
      .map(_ => RefinedTypeError("must be a string between 1 and 1000 characters, not all of them white", name))

}
