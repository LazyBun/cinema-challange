package pl.piszkod.cinema.service

import cats.effect.{IO, Ref}
import cats.syntax.all.*
import cats.data.EitherT
import cats.effect.std.Semaphore
import pl.piszkod.cinema.domain.{Block, Movie, Room, Schedule, Show, Slot}

import java.time.ZonedDateTime

object ScheduleManager {
  sealed trait Error

  object Error {
    case class ScheduleRepositoryError(error: ScheduleRepository.Error)
        extends ScheduleManager.Error

    case class NewSlotCollides(newSlot: Slot, existingSlot: Slot)
        extends ScheduleManager.Error

    case class CannotSchedulePremiereOutsideHours(show: Show)
        extends ScheduleManager.Error
  }
}

class ScheduleManager(
    val scheduleRepository: ScheduleRepository,
    // TODO: We could do a semaphore per room in schedule, but for simplicity's sake I'll do a single per schedule
    s: Semaphore[IO]
) {

  // TODO: This should be limited by date/paged/whatever we would need to do in order not to return entire schedule
  //   For simplicity's sake, I'm going to just return the entire schedule
  def get(): EitherT[IO, ScheduleManager.Error, Schedule] =
    for {
      schedule <- scheduleRepository.getAll.leftMap(
        ScheduleManager.Error.ScheduleRepositoryError.apply
      )
    } yield schedule

  def scheduleSlot(
      room: Room.Name,
      newSlot: Slot
  ): EitherT[IO, ScheduleManager.Error, Unit] =
    for {
      _ <- checkPremiereRestriction(newSlot)
      _ <- EitherT(trySchedule(room, newSlot))
    } yield ()

  private def checkPremiereRestriction(newSlot: Slot) =
    // FIXME make prettier
    newSlot match {
      case show @ Show(
            _,
            Movie(_, _, _, kind: Movie.Kind.Premiere.type, _),
            _
          ) =>
        if (
          newSlot.start.value.getHour < 17 || newSlot.start.value.getHour > 21
        ) {
          EitherT.leftT[IO, Unit](
            ScheduleManager.Error.CannotSchedulePremiereOutsideHours(show)
          )
        } else {
          EitherT.pure[IO, ScheduleManager.Error](())
        }
      case _ => EitherT.pure[IO, ScheduleManager.Error](())
    }

  private def trySchedule(
      room: Room.Name,
      newSlot: Slot
  ) = s.permit.use { _ =>
    (for {
      existingSlots <- scheduleRepository
        .getRoom(room)
        .leftMap(ScheduleManager.Error.ScheduleRepositoryError.apply)
      // TODO: This could return all collisions instead of first one. For simplicity, I'm just returning first collision
      _ <- existingSlots.traverse_ { existingSlot =>
        if (existingSlot.collides(newSlot)) {
          EitherT.leftT[IO, Unit](
            ScheduleManager.Error.NewSlotCollides(newSlot, existingSlot)
          )
        } else {
          EitherT.right(IO.unit)
        }
      }
      _ <- scheduleRepository
        .add(room, newSlot)
        .leftMap(ScheduleManager.Error.ScheduleRepositoryError.apply)
    } yield ()).value
  }

}
