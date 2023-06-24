package pl.piszkod.cinema.service

import cats.data.EitherT
import cats.effect.IO
import pl.piszkod.cinema.domain.{Movie, Room}
import pl.piszkod.cinema.service

trait RoomRepository {

  def get(name: Room.Name): EitherT[IO, RoomRepository.Error, Room]

}

object RoomRepository {
  sealed trait Error

  object Error {

    case class RoomNotFound(name: Room.Name)
        extends service.RoomRepository.Error

  }
}

class InMemoryRoomRepository(val db: Map[Room.Name, Room])
    extends RoomRepository {
  override def get(name: Room.Name): EitherT[IO, RoomRepository.Error, Room] =
    EitherT.fromOption[IO](
      db.get(name),
      RoomRepository.Error.RoomNotFound(name)
    )
}
