package zio.experiment.domain.service

import zio.experiment.domain.model.AppError
import zio.experiment.domain.model.User.User
import zio.experiment.domain.port.UserPersistence
import zio.{RIO, ZIO}

object UserService {
  def getUser(id: Int): ZIO[UserPersistence, AppError, User] = RIO.accessM(_.get.get(id))
  def createUser(id: Int, name: String): ZIO[UserPersistence, AppError, User] =
    for {
      user   <- ZIO.fromEither(User(id, name))
      stored <- RIO.accessM[UserPersistence](_.get.create(user))
    } yield stored

  def deleteUser(id: Int): RIO[UserPersistence, Boolean] = RIO.accessM(_.get.delete(id))
}
