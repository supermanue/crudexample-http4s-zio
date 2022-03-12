package zio.experiment

import doobie.util.transactor.Transactor
import zio.{Has, Task}

package object adapters {
  type DBTransactor = Has[Transactor[Task]]
}
