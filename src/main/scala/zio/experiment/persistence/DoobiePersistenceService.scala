package zio.experiment.persistence

import cats.effect.Blocker
import doobie.h2.H2Transactor
import doobie.implicits._
import doobie.{Query0, Transactor, Update0}
import zio._
import zio.blocking.Blocking
import zio.experiment.configuration
import zio.experiment.configuration.DbConfig
import zio.experiment.domain.model.User.User
import zio.experiment.domain.model.{AppError, DBError, UserNotFound}
import zio.experiment.domain.port.{StoragePort, UserPersistence}
import zio.experiment.persistence.DataTypes.UserStored
import zio.interop.catz._

import scala.concurrent.ExecutionContext

//TODO move this to somewhere cleaner
object DataTypes {
  case class UserStored(id: Int, name: String)
}

/**
  * Persistence Module for production using Doobie
  */
final class DoobiePersistenceService(tnx: Transactor[Task]) extends StoragePort {
  import DoobiePersistenceService._

  def get(id: Int): IO[AppError, User] =
    SQL
      .get(id)
      .option
      .transact(tnx)
      .foldM( //TODO make this cleaner
        err => IO.fail(DBError(err.getMessage)),
        _.fold[IO[AppError, User]](IO.fail(UserNotFound(id): AppError)) { storedUser =>
          User
            .build(storedUser.id, storedUser.name)
            .fold[IO[AppError, User]](
              err => IO.fail(err),
              u => Task.succeed(u)
            )
        }
      )

  def create(user: User): IO[AppError, User] =
    SQL
      .create(UserStored(user.id.value, user.name.value))
      .run
      .transact(tnx)
      .foldM(err => IO.fail(DBError(err.getMessage)), _ => IO.succeed(user))

  def delete(id: Int): Task[Boolean] =
    SQL
      .delete(id)
      .run
      .transact(tnx)
      .fold(
        _ => false,
        _ => true
      ) //TODO does not distinguish between "user not found" and "there is an issue with the DB"
}

object DoobiePersistenceService {

  object SQL {

    def get(id: Int): Query0[UserStored] =
      sql"""SELECT * FROM USERS WHERE ID = $id """.query[UserStored]

    def create(user: UserStored): Update0 =
      sql"""INSERT INTO USERS (id, name) VALUES (${user.id}, ${user.name})""".update

    def delete(id: Int): Update0 =
      sql"""DELETE FROM USERS WHERE id = $id""".update

    def createUsersTable: doobie.Update0 =
      sql"""CREATE TABLE USERS (id Int, name VARCHAR NOT NULL)""".update

    def dropUsersTable: doobie.Update0 =
      sql"""DROP TABLE IF EXISTS USERS""".update
  }

  def createUserTable: ZIO[DBTransactor, Throwable, Unit] =
    for {
      tnx <- ZIO.service[Transactor[Task]]
      _   <- SQL.createUsersTable.run.transact(tnx)
    } yield ()

  def dropUserTable: ZIO[DBTransactor, Throwable, Unit] =
    for {
      tnx <- ZIO.service[Transactor[Task]]
      _   <- SQL.dropUsersTable.run.transact(tnx)
    } yield ()

  def mkTransactor(
      conf: DbConfig,
      connectEC: ExecutionContext,
      transactEC: ExecutionContext
  ): Managed[Throwable, Transactor[Task]] = {
    import zio.interop.catz._

    H2Transactor
      .newH2Transactor[Task](
        conf.url,
        conf.user,
        conf.password,
        connectEC,
        Blocker.liftExecutionContext(transactEC)
      )
      .toManagedZIO
  }

  val transactorLive: ZLayer[Has[DbConfig] with Blocking, Throwable, DBTransactor] =
    ZLayer.fromManaged(for {
      config     <- configuration.dbConfig.toManaged_
      connectEC  <- ZIO.descriptor.map(_.executor.asEC).toManaged_
      blockingEC <- blocking.blocking { ZIO.descriptor.map(_.executor.asEC) }.toManaged_
      transactor <- mkTransactor(config, connectEC, blockingEC)
    } yield transactor)

  val live: ZLayer[DBTransactor, Throwable, UserPersistence] =
    ZLayer.fromService(new DoobiePersistenceService(_))

}
