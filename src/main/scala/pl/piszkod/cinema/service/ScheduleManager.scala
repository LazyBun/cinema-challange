package pl.piszkod.cinema.service

import cats.effect.{IO, Ref}
import cats.syntax.all.*
import cats.data.EitherT
import cats.effect.std.Semaphore
import pl.piszkod.cinema.domain.{Block, Movie, Room, Schedule, Show, Slot}
import pl.piszkod.cinema.repository.{RoomRepository, ScheduleRepository}
import pl.piszkod.cinema.service
import pl.piszkod.cinema.service.ScheduleManager.Error

import java.time.ZonedDateTime

object ScheduleManager {

  def build(
      scheduleRepository: ScheduleRepository,
      roomRepository: RoomRepository,
      movieCatalog: MovieCatalog,
      semaphore: IO[Semaphore[IO]] = Semaphore[IO](1)
  ) = semaphore.map(
    new ScheduleManager(
      scheduleRepository,
      roomRepository,
      movieCatalog,
      _
    )
  )

  sealed trait Error

  object Error {
    case class ScheduleRepositoryError(error: ScheduleRepository.Error)
        extends ScheduleManager.Error

    case class MovieCatalogError(error: MovieCatalog.Error)
        extends ScheduleManager.Error

    case class RoomRepositoryError(error: RoomRepository.Error)
        extends ScheduleManager.Error

    case class RoomNot3D(room: Room.Name, movie: Movie.Uid)
        extends ScheduleManager.Error

    case class NewSlotCollidesWithExistingRoomSchedule(
        newSlot: Slot,
        existingSlot: Slot
    ) extends ScheduleManager.Error

    case class CannotSchedulePremiereOutsideHours(show: Show)
        extends ScheduleManager.Error

    case class CannotScheduleShowOutsideWorkingHours(show: Show)
        extends ScheduleManager.Error
  }
}

class ScheduleManager(
    val scheduleRepository: ScheduleRepository,
    val roomRepository: RoomRepository,
    val movieCatalog: MovieCatalog,
    // TODO: We could do a semaphore per room in schedule, but for simplicity's sake I'll do a single per schedule
    s: Semaphore[IO]
) {

  // TODO: This should be limited by date/paged/whatever we would need to do in order not to return entire schedule
  //   For simplicity's sake, I'm going to just return the entire schedule
  def get(): EitherT[IO, ScheduleManager.Error, Schedule] =
    for {
      schedule <- scheduleRepository.getEntireSchedule.leftMap(
        ScheduleManager.Error.ScheduleRepositoryError.apply
      )
    } yield schedule

  def scheduleBlock(
      room: Room.Name,
      startTime: Slot.Start,
      blockTime: Block.Time
  ): EitherT[IO, ScheduleManager.Error, Unit] = for {
    room <- roomRepository
      .get(room)
      .leftMap(
        ScheduleManager.Error.RoomRepositoryError.apply
      )
    newBlock = Block(
      start = startTime,
      blockTime = blockTime
    )
    _ <- trySchedule(room, newBlock)
  } yield ()

  def scheduleShow(
      room: Room.Name,
      startTime: Slot.Start,
      movieUid: Movie.Uid
  ): EitherT[IO, ScheduleManager.Error, Unit] =
    for {
      room <- roomRepository
        .get(room)
        .leftMap(
          ScheduleManager.Error.RoomRepositoryError.apply
        )
      movie <- movieCatalog
        .get(movieUid)
        .leftMap(
          ScheduleManager.Error.MovieCatalogError.apply
        )
      newShow = Show(
        start = startTime,
        movieSnapshot = movie,
        roomCleaningTimeSnapshot = Show.CleaningTime(room.cleaningTime.value)
      )
      _ <- check3DRestriction(movie, room)
      _ <- checkCinemaWorkingHoursRestriction(newShow)
      _ <- checkPremiereRestriction(newShow)
      _ <- trySchedule(room, newShow)
    } yield ()

  private def check3DRestriction(movie: Movie, room: Room) =
    if (
      movie.requirements.contains(
        Movie.Requirement.Glasses3D
      ) && !room.capabilities.contains(Room.Capability.ThreeD)
    ) {
      EitherT.leftT[IO, Unit](
        ScheduleManager.Error.RoomNot3D(room.name, movie.uid)
      )
    } else {
      EitherT.rightT[IO, ScheduleManager.Error](())
    }

  private def checkCinemaWorkingHoursRestriction(show: Show) =
    if (
      // TODO: Those values should be in the config, but for simplicity I'm keeping them as magic numbers
      show.start.value.getHour < 8 ||
      show.start.value.getHour >= 22 ||
      show.end.value.getHour < 8 ||
      show.end.value.getHour >= 22
    ) {
      EitherT.leftT[IO, Unit](
        ScheduleManager.Error.CannotScheduleShowOutsideWorkingHours(show)
      )
    } else {
      EitherT.rightT[IO, ScheduleManager.Error](())
    }

  private def checkPremiereRestriction(show: Show) =
    if (
      // TODO: Those values should be in the config, but for simplicity I'm keeping them as magic numbers
      show.movieSnapshot.kind == Movie.Kind.Premiere && (show.start.value.getHour < 17 || show.start.value.getHour >= 21)
    ) {
      EitherT.leftT[IO, Unit](
        ScheduleManager.Error.CannotSchedulePremiereOutsideHours(show)
      )
    } else {
      EitherT.rightT[IO, ScheduleManager.Error](())
    }

  private def trySchedule(
      room: Room,
      newSlot: Slot
  ) = EitherT.apply(s.permit.use { _ =>
    (for {
      existingSlots <- scheduleRepository
        .getRoomSchedule(room.name)
        .leftMap(ScheduleManager.Error.ScheduleRepositoryError.apply)
      // TODO: This could return all collisions instead of first one. For simplicity, I'm just checking for the first collision
      _ <- existingSlots.traverse_ { existingSlot =>
        if (existingSlot.collides(newSlot)) {
          EitherT.leftT[IO, Unit](
            ScheduleManager.Error
              .NewSlotCollidesWithExistingRoomSchedule(newSlot, existingSlot)
          )
        } else {
          EitherT.rightT[IO, ScheduleManager.Error](())
        }
      }
      _ <- scheduleRepository
        .addSlotToSchedule(room.name, newSlot)
        .leftMap(ScheduleManager.Error.ScheduleRepositoryError.apply)
    } yield ()).value
  })

}
