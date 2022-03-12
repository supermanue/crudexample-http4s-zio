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
