package zio.experiment.domain

import zio.Has

package object port {
  type UserPersistence = Has[StoragePort]
}
