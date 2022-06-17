package zio.experiment.domain.service

import zio.experiment.domain.model.AppError
import zio.experiment.domain.model.User.User
import zio.experiment.domain.port.UserRepository
import zio.experiment.domain.port.UserRepository.UserRepositoryEnv
import zio.{RIO, ZIO}

object UserService {
  def getUser(id: Int): ZIO[UserRepositoryEnv, AppError, User] = UserRepository.get(id)
  def createUser(id: Int, name: String): ZIO[UserRepositoryEnv, AppError, User] =
    for {
      user   <- ZIO.fromEither(User.build(id, name))
      stored <- UserRepository.create(user)
    } yield stored
  def deleteUser(id: Int): RIO[UserRepositoryEnv, Boolean] = UserRepository.delete(id)
}
