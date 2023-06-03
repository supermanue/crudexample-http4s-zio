package zio.experiment.http

import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes}
import zio._
import zio.experiment.domain.model.{DBError, RefinedTypeError, UserNotFound}
import zio.experiment.domain.port.UserRepository.UserRepositoryEnv
import zio.experiment.domain.service.UserService
import zio.experiment.http.model.UserInput
import zio.experiment.http.model.UserInput.encodeUser
import zio.interop.catz._

//FIXME this restriction here should be removed, the API converted to an object and use ZLayers for dependency injection
final case class Api[R <: UserRepositoryEnv](rootUri: String) {

  type UserTask[A] = RIO[R, A]

  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]): EntityDecoder[UserTask, A] = jsonOf[UserTask, A]
  implicit def circeJsonEncoder[A](implicit decoder: Encoder[A]): EntityEncoder[UserTask, A] =
    jsonEncoderOf[UserTask, A]

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
