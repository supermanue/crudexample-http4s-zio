package zio.experiment.domain.service

import zio.experiment.domain.model.{AppError, User}
import zio.experiment.domain.port.UserPersistence
import zio.{RIO, ZIO}

object UserService {
  def getUser(id: Int): ZIO[UserPersistence, AppError, User]    = RIO.accessM(_.get.get(id))
  def createUser(a: User): ZIO[UserPersistence, AppError, User] = RIO.accessM(_.get.create(a))
  def deleteUser(id: Int): RIO[UserPersistence, Boolean]        = RIO.accessM(_.get.delete(id))
}
