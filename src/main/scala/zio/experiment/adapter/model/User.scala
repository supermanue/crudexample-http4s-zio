package zio.experiment.adapter.model

import zio.experiment.domain.model.AppError
import zio.experiment.domain.model.User.{User => UserDomain}

case class User(id: Int, name: String)

object User {
  implicit class UserConversions(user: User) {
    def toDomainUser: Either[AppError, UserDomain] =
      UserDomain.build(user.id, user.name)
  }

  def fromDomainUser(domainUser: UserDomain): User =
    User(domainUser.id.value, domainUser.name.value)
}
