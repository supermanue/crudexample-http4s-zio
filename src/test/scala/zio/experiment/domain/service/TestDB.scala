package zio.experiment.domain.service

import zio.experiment.domain.model.{AppError, User, UserNotFound}
import zio.experiment.domain.port.{StoragePort, UserPersistence}
import zio.{IO, Ref, Task, ZLayer}

case class TestDB(users: Ref[Vector[User]]) extends StoragePort {
  def get(id: Int): IO[AppError, User] =
    users.get.flatMap(users => IO.require(UserNotFound(id))(Task.succeed(users.find(_.id == id))))
  def create(user: User): IO[AppError, User] =
    users.update(_ :+ user).map(_ => user)
  def delete(id: Int): Task[Boolean] =
    users.modify(users => true -> users.filterNot(_.id == id))
}

object TestDB {
  val layer: ZLayer[Any, Nothing, UserPersistence] =
    ZLayer.fromEffect(Ref.make(Vector.empty[User]).map(TestDB(_)))

}
