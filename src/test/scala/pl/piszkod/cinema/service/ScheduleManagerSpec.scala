package pl.piszkod.cinema.service

import cats.effect.{IO, Ref}
import munit.CatsEffectSuite
import pl.piszkod.cinema.domain.{Block, Movie, Room, Schedule, Show, Slot}
import pl.piszkod.cinema.repository.{
  InMemoryRoomRepository,
  InMemoryScheduleRepository
}

import java.time.{Duration, LocalDate, LocalTime, ZoneId, ZonedDateTime}

class ScheduleManagerSpec extends CatsEffectSuite {

  val room3d = Room(
    name = Room.Name("room3d"),
    cleaningTime = Room.CleaningTime(Duration.ofHours(1)),
    capabilities = Seq(Room.Capability.ThreeD)
  )

  val roomNo3d = Room(
    name = Room.Name("roomNo3d"),
    cleaningTime = Room.CleaningTime(Duration.ofHours(1)),
    capabilities = Seq()
  )

  val movie = Movie(
    uid = Movie.Uid("1"),
    title = Movie.Title("Bee movie"),
    length = Movie.Length(Duration.ofMinutes(90)),
    kind = Movie.Kind.Regular,
    requirements = Seq()
  )

  val moviePremiere = movie.copy(
    uid = Movie.Uid("2"),
    kind = Movie.Kind.Premiere
  )

  val movie3d = movie.copy(
    uid = Movie.Uid("3"),
    requirements = Seq(Movie.Requirement.Glasses3D)
  )

  val localDate = LocalDate.of(2137, 4, 20)
  val zoneId = ZoneId.of("UTC")

  test("Should schedule show") {
    for {
      (scheduleManager, scheduleRef) <- createScheduleManagerWithSchedule(
        Map(
          room3d.name -> Seq()
        )
      )
      result <- scheduleManager
        .scheduleShow(
          room3d.name,
          Slot.Start(ZonedDateTime.of(localDate, LocalTime.of(12, 0), zoneId)),
          movie.uid
        )
        .value
      contents <- scheduleRef.get
    } yield {
      result match {
        case Left(value) =>
          fail(value.toString)
        case _ =>
          val show = contents(room3d.name).head.asInstanceOf[Show]
          assertEquals(movie, show.movieSnapshot)
          assertEquals(
            show.roomCleaningTimeSnapshot.value,
            room3d.cleaningTime.value
          )
      }
    }
  }

  test(
    "Should schedule show if tries to schedule premiere show in correct hours"
  ) {
    for {
      (scheduleManager, scheduleRef) <- createScheduleManagerWithSchedule(
        Map(
          room3d.name -> Seq()
        )
      )

      result <- scheduleManager
        .scheduleShow(
          room3d.name,
          Slot.Start(ZonedDateTime.of(localDate, LocalTime.of(18, 0), zoneId)),
          moviePremiere.uid
        )
        .value
      contents <- scheduleRef.get
    } yield {
      result match {
        case Left(value) =>
          fail(value.toString)
        case _ =>
          val show = contents(room3d.name).head.asInstanceOf[Show]
          assertEquals(moviePremiere, show.movieSnapshot)
          assertEquals(
            show.roomCleaningTimeSnapshot.value,
            room3d.cleaningTime.value
          )
      }
    }
  }

  test("Should fail if tries to schedule premiere show for wrong hours") {
    for {
      (scheduleManager, scheduleRef) <- createScheduleManagerWithSchedule(
        Map(
          room3d.name -> Seq()
        )
      )

      result <- scheduleManager
        .scheduleShow(
          room3d.name,
          Slot.Start(ZonedDateTime.of(localDate, LocalTime.of(12, 0), zoneId)),
          moviePremiere.uid
        )
        .value
    } yield {
      result match {
        case Left(
              err: ScheduleManager.Error.CannotSchedulePremiereOutsideHours
            ) =>
          ()
        case _ =>
          fail("unexpected result")
      }
    }
  }

  test("Should fail if tries to schedule show outside cinema working hours") {
    for {
      (scheduleManager, scheduleRef) <- createScheduleManagerWithSchedule(
        Map(
          room3d.name -> Seq()
        )
      )

      resultBefore <- scheduleManager
        .scheduleShow(
          room3d.name,
          Slot.Start(ZonedDateTime.of(localDate, LocalTime.of(2, 0), zoneId)),
          movie.uid
        )
        .value
      resultAfter <- scheduleManager
        .scheduleShow(
          room3d.name,
          Slot.Start(ZonedDateTime.of(localDate, LocalTime.of(23, 0), zoneId)),
          movie.uid
        )
        .value
    } yield {
      (resultBefore, resultAfter) match
        case (Left(err: ScheduleManager.Error.CannotScheduleShowOutsideWorkingHours), Left(er: ScheduleManager.Error.CannotScheduleShowOutsideWorkingHours)) => ()
        case _ => fail("unexpected result")
    }
  }

  test("Should schedule show if tries to schedule 3d show in 3d room") {
    for {
      (scheduleManager, scheduleRef) <- createScheduleManagerWithSchedule(
        Map(
          room3d.name -> Seq()
        )
      )

      result <- scheduleManager
        .scheduleShow(
          room3d.name,
          Slot.Start(ZonedDateTime.of(localDate, LocalTime.of(18, 0), zoneId)),
          movie3d.uid
        )
        .value
      contents <- scheduleRef.get
    } yield {
      result match {
        case Left(value) =>
          fail(value.toString)
        case _ =>
          val show = contents(room3d.name).head.asInstanceOf[Show]
          assertEquals(movie3d, show.movieSnapshot)
          assertEquals(
            show.roomCleaningTimeSnapshot.value,
            room3d.cleaningTime.value
          )
      }
    }
  }

  test("Should fail if tries to schedule 3d show in non-3d room") {
    for {
      (scheduleManager, scheduleRef) <- createScheduleManagerWithSchedule(
        Map(
          roomNo3d.name -> Seq()
        )
      )

      result <- scheduleManager
        .scheduleShow(
          roomNo3d.name,
          Slot.Start(ZonedDateTime.of(localDate, LocalTime.of(18, 0), zoneId)),
          movie3d.uid
        )
        .value
    } yield {
      result match {
        case Left(err: ScheduleManager.Error.RoomNot3D) =>
          ()
        case _ =>
          fail("unexpected result")
      }
    }
  }

  test("Should fail when new slot collides with existing one") {
    for {
      (scheduleManager, scheduleRef) <- createScheduleManagerWithSchedule(
        Map(
          roomNo3d.name -> Seq(
            Block(
              Slot.Start(
                ZonedDateTime.of(localDate, LocalTime.of(12, 0), zoneId)
              ),
              Block.Time(Duration.ofHours(6))
            )
          )
        )
      )

      result <- scheduleManager
        .scheduleShow(
          roomNo3d.name,
          Slot.Start(ZonedDateTime.of(localDate, LocalTime.of(14, 0), zoneId)),
          movie.uid
        )
        .value
    } yield {
      result match {
        case Left(
              err: ScheduleManager.Error.NewSlotCollidesWithExistingRoomSchedule
            ) =>
        // Success
        case _ =>
          fail("unexpected result")
      }
    }
  }

  // TODO: How to test?
  test("Should not allow for concurrent schedule modifications") {}

  test("Should schedule block") {
    for {
      (scheduleManager, scheduleRef) <- createScheduleManagerWithSchedule(
        Map(
          room3d.name -> Seq()
        )
      )
      blockStart = ZonedDateTime.of(localDate, LocalTime.of(12, 0), zoneId)
      blockEnd = ZonedDateTime.of(localDate, LocalTime.of(18, 0), zoneId)
      result <- scheduleManager
        .scheduleBlock(
          room3d.name,
          Slot.Start(ZonedDateTime.of(localDate, LocalTime.of(12, 0), zoneId)),
          Block.Time(Duration.ofHours(6))
        )
        .value
      contents <- scheduleRef.get
    } yield {
      result match {
        case Left(value) =>
          fail(value.toString)
        case _ =>
          val block = contents(room3d.name).head.asInstanceOf[Block]
          assertEquals(block.start.value, blockStart)
          assertEquals(
            block.end.value,
            blockEnd
          )
      }
    }
  }

  private def createScheduleManagerWithSchedule(
      schedule: Schedule
  ): IO[(ScheduleManager, Ref[IO, Schedule])] = for {
    scheduleRef <- Ref.of[IO, Schedule](schedule)
    movieCatalog <- IO.pure(createMovieCatalog())
    roomRepository = createRoomRepository()
    scheduleRepository = createScheduleRepository(scheduleRef)
    scheduleManager <- ScheduleManager.build(
      scheduleRepository,
      roomRepository,
      movieCatalog
    )
  } yield (scheduleManager, scheduleRef)

  private def createScheduleRepository(
      ref: Ref[IO, Schedule]
  ) = new InMemoryScheduleRepository(ref)

  private def createMovieCatalog(
      catalog: Map[Movie.Uid, Movie] = Map(
        movie.uid -> movie,
        moviePremiere.uid -> moviePremiere,
        movie3d.uid -> movie3d
      )
  ) = new InMemoryMovieCatalog(catalog)

  private def createRoomRepository(
      rooms: Map[Room.Name, Room] = Map(
        room3d.name -> room3d,
        roomNo3d.name -> roomNo3d
      )
  ) = new InMemoryRoomRepository(rooms)

}
