package zio.experiment.http

import io.circe.generic.auto._
import io.circe.{ Decoder, Encoder, Json }
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{ EntityDecoder, EntityEncoder, HttpRoutes }
import zio._
import zio.experiment.domain.model.User.User
import zio.experiment.domain.model.{ DBError, RefinedTypeError, UserNotFound }
import zio.experiment.domain.port.UserPersistence
import zio.experiment.domain.service.UserService
import zio.interop.catz._

//TODO do we really want/need to have the UserPersistence injected here? Is it required for the Domain or can we inject it somehow differently?
final case class Api[R <: UserPersistence](rootUri: String) {

  type UserTask[A] = RIO[R, A]

  case class UserInput(id: Int, name: String) //TODO this is a bit weird here. Also id should be returned by Storage

  //TODO take this to a separate class
  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]): EntityDecoder[UserTask, A] = jsonOf[UserTask, A]
  implicit def circeJsonEncoder[A](implicit decoder: Encoder[A]): EntityEncoder[UserTask, A] =
    jsonEncoderOf[UserTask, A]
  implicit val encodeUser: Encoder[User] = new Encoder[User] {
    final def apply(a: User): Json =
      Json.obj(
        ("id", Json.fromInt(a.id.value)),
        ("name", Json.fromString(a.name.value))
      )
  }

  val dsl: Http4sDsl[UserTask] = Http4sDsl[UserTask]
  import dsl._

  val route: HttpRoutes[UserTask] = {
    HttpRoutes.of[UserTask] {
      case GET -> Root / "healthcheck" => NoContent()
      case GET -> Root / IntVar(id) =>
        UserService
          .getUser(id)
          .foldM(
            {
              case error: UserNotFound => NotFound(error.message)
              case error: DBError      => ServiceUnavailable(error.message)
              case other               => InternalServerError(other.message)
            },
            Ok(_)
          )
      case request @ POST -> Root =>
        request.decode[UserInput] { userInput =>
          UserService
            .createUser(userInput.id, userInput.name)
            .foldM(
              {
                case error: DBError          => ServiceUnavailable(error.message)
                case error: RefinedTypeError => BadRequest(error.message)
                case other                   => InternalServerError(other.message)
              },
              Created(_)
            )
        }
      case DELETE -> Root / IntVar(id) =>
        (UserService.getUser(id) *> UserService.deleteUser(id))
          .foldM(_ => NotFound(), Ok(_)) //See note in DoobiePersistenceService
    }
  }

}
