package pl.piszkod.cinema.service

import cats.data.EitherT
import cats.effect.{IO, Ref}
import pl.piszkod.cinema.domain.{Room, RoomSchedule, Schedule, Slot}

trait ScheduleRepository {

  def getRoom(
      room: Room.Name
  ): EitherT[IO, ScheduleRepository.Error, RoomSchedule]

  def getAll: EitherT[IO, ScheduleRepository.Error, Schedule]

  def add(
      room: Room.Name,
      slot: Slot
  ): EitherT[IO, ScheduleRepository.Error, Unit]

}

object ScheduleRepository {

  sealed trait Error

  object Error {

    case class CouldNotFindRoom(room: Room.Name)
        extends ScheduleRepository.Error

  }
}

class InMemoryRepository(val scheduleRef: Ref[IO, Schedule])
    extends ScheduleRepository {
  override def getRoom(
      room: Room.Name
  ): EitherT[IO, ScheduleRepository.Error, RoomSchedule] =
    for {
      schedule <- EitherT.right(scheduleRef.get)
      roomSchedule <- EitherT.fromOption(
        schedule.get(room),
        ScheduleRepository.Error.CouldNotFindRoom(room)
      )
    } yield roomSchedule

  override def getAll: EitherT[IO, ScheduleRepository.Error, Schedule] =
    EitherT.right(scheduleRef.get)

  override def add(
      room: Room.Name,
      newSlot: Slot
  ): EitherT[IO, ScheduleRepository.Error, Unit] =
    for {
      existingRoomSchedule <- getRoom(room)
      _ <- EitherT.right(
        scheduleRef.update(previousSchedule =>
          previousSchedule.updated(room, existingRoomSchedule.appended(newSlot))
        )
      )
    } yield ()
}
