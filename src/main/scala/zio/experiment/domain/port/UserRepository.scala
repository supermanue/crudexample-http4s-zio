package zio.experiment.domain.port

import zio.experiment.domain.model.AppError
import zio.experiment.domain.model.User.User
import zio.{Has, IO, RIO, Task, ZIO}

object UserRepository {
  //type definition
  type UserRepositoryEnv = Has[UserRepository.Service]

  //service def
  trait Service {
    def get(id: Int): IO[AppError, User]
    def create(a: User): IO[AppError, User]
    def delete(id: Int): Task[Boolean]
  }

  //front facing API
  def get(id: Int): ZIO[UserRepositoryEnv, AppError, User]       = ZIO.accessM[UserRepositoryEnv](_.get.get(id))
  def create(user: User): ZIO[UserRepositoryEnv, AppError, User] = ZIO.accessM[UserRepositoryEnv](_.get.create(user))
  def delete(id: Int): RIO[UserRepositoryEnv, Boolean]           = ZIO.accessM[UserRepositoryEnv](_.get.delete(id))

}
