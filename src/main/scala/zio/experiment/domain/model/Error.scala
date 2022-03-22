package zio.experiment.domain.model

trait AppError {
  val message: String
}

final case class DBError(error: String) extends AppError {
  val message = s"DBError: $error"
}

final case class UserNotFound(id: Int) extends AppError {
  val message = s"User with id $id not found"
}

final case class IncorrectId(id: Int) extends AppError {
  val message = s"$id  must be a positive integer"
}

final case class IncorrectName(name: String) extends AppError {
  val message = s"$name  must be a nonemtpy string"
}

final case class RefinedTypeError(msg: String, param: String) extends AppError {
  val message = s" error with param $param: $msg"
}
