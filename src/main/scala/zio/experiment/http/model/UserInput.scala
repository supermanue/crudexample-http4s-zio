package zio.experiment.http.model

import io.circe.{Encoder, Json}
import zio.experiment.domain.model.User.User

case class UserInput(id: Int, name: String)

object UserInput {
  implicit val encodeUser: Encoder[User] = new Encoder[User] {
    final def apply(a: User): Json =
      Json.obj(
        ("id", Json.fromInt(a.id.value)),
        ("name", Json.fromString(a.name.value))
      )
  }
}
