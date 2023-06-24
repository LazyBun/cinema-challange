package pl.piszkod.cinema.repository

import cats.data.EitherT
import cats.effect.{IO, Ref}
import pl.piszkod.cinema.domain.{Room, RoomSchedule, Schedule, Slot}
import pl.piszkod.cinema.repository.ScheduleRepository

trait ScheduleRepository {

  def getRoomSchedule(
      room: Room.Name
  ): EitherT[IO, ScheduleRepository.Error, RoomSchedule]

  def getEntireSchedule: EitherT[IO, ScheduleRepository.Error, Schedule]

  def addSlotToSchedule(
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

class InMemoryScheduleRepository(val scheduleRef: Ref[IO, Schedule])
    extends ScheduleRepository {
  override def getRoomSchedule(
      room: Room.Name
  ): EitherT[IO, ScheduleRepository.Error, RoomSchedule] =
    for {
      schedule <- EitherT.right(scheduleRef.get)
      roomSchedule <- EitherT.fromOption(
        schedule.get(room),
        ScheduleRepository.Error.CouldNotFindRoom(room)
      )
    } yield roomSchedule

  override def getEntireSchedule
      : EitherT[IO, ScheduleRepository.Error, Schedule] =
    EitherT.right(scheduleRef.get)

  override def addSlotToSchedule(
      room: Room.Name,
      newSlot: Slot
  ): EitherT[IO, ScheduleRepository.Error, Unit] =
    for {
      // TODO: Handle case where there is no schedule for the room yet
      existingRoomSchedule <- getRoomSchedule(room)
      _ <- EitherT.right(
        scheduleRef.update(previousSchedule =>
          previousSchedule.updated(room, existingRoomSchedule.appended(newSlot))
        )
      )
    } yield ()
}
