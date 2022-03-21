package zio.experiment.domain.port

import zio.experiment.domain.model.AppError
import zio.experiment.domain.model.User.User
import zio.{IO, Task}

trait StoragePort {
  def get(id: Int): IO[AppError, User]
  def create(a: User): IO[AppError, User]
  def delete(id: Int): Task[Boolean]
}
