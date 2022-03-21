package zio.experiment.domain.model

object User { //TODO package name?
  sealed abstract case class User private (
      id: Int,
      name: String
  ) //TODO: use Refined Types here so we restrict the "name" String characteristics (min, max, forbidden chars)
  object User {
    def apply(id: Int, name: String): Either[AppError, User] = {
      (id, name) match {
        case (i, _) if i < 0     => Left(IncorrectId(id))
        case (_, n) if n.isEmpty => Left(IncorrectName(name))
        case _                   => Right(new User(id, name) {})
      }
    }
  }
}
