package pl.piszkod.cinema.domain

import munit.CatsEffectSuite

import java.time.{LocalDate, LocalTime, ZoneId, ZonedDateTime}

class SlotSpec extends CatsEffectSuite {

  val localDate = LocalDate.of(2137, 4, 20)
  val zoneId = ZoneId.of("UTC")

  test("should not collide if slot2 is outside slot1 (before)") {
    val collides = timeSlot(
      startTime = LocalTime.of(12, 0),
      endTime = LocalTime.of(13, 0)
    ).collides(
      timeSlot(startTime = LocalTime.of(10, 0), endTime = LocalTime.of(11, 0))
    )

    assert(!collides)
  }

  test("should not collide if slot2 is outside slot1 (after)") {
    val collides = timeSlot(
      startTime = LocalTime.of(12, 0),
      endTime = LocalTime.of(13, 0)
    ).collides(
      timeSlot(startTime = LocalTime.of(14, 0), endTime = LocalTime.of(15, 0))
    )

    assert(!collides)
  }

  test("should not collide if slot2 is outside slot1 (touching endTime and startTime)") {
    val collides = timeSlot(
      startTime = LocalTime.of(12, 0),
      endTime = LocalTime.of(13, 0)
    ).collides(
      timeSlot(startTime = LocalTime.of(10, 0), endTime = LocalTime.of(12, 0))
    )

    assert(!collides)
  }

  test("should collide if slot2 overlaps slot1 (slot2 endTime is after slot1 startTime)") {
    val collides = timeSlot(
      startTime = LocalTime.of(12, 0),
      endTime = LocalTime.of(13, 0)
    ).collides(
      timeSlot(startTime = LocalTime.of(10, 0), endTime = LocalTime.of(12, 30))
    )

    assert(collides)
  }

  test("should collide if slot2 overlaps slot1 (slot2 startTime is inside slo1 and endTime is after slot1 endTime)") {
    val collides = timeSlot(
      startTime = LocalTime.of(12, 0),
      endTime = LocalTime.of(13, 0)
    ).collides(
      timeSlot(startTime = LocalTime.of(12, 0), endTime = LocalTime.of(14, 0))
    )

    assert(collides)
  }

  test("should not collide if slot2 is outside slot1 (touching startTime and endTime)") {
    val collides = timeSlot(
      startTime = LocalTime.of(12, 0),
      endTime = LocalTime.of(13, 0)
    ).collides(
      timeSlot(startTime = LocalTime.of(13, 0), endTime = LocalTime.of(15, 0))
    )

    assert(!collides)
  }

  test("should collide if slot2 is over slot1") {
    val collides = timeSlot(
      startTime = LocalTime.of(12, 0),
      endTime = LocalTime.of(13, 0)
    ).collides(
      timeSlot(startTime = LocalTime.of(10, 0), endTime = LocalTime.of(15, 0))
    )

    assert(collides)
  }

  // TODO: better name
  private def timeSlot(startTime: LocalTime, endTime: LocalTime) = slot(
    startTime = ZonedDateTime.of(localDate, startTime, zoneId),
    endTime = ZonedDateTime.of(localDate, endTime, zoneId)
  )

  private def slot(startTime: ZonedDateTime, endTime: ZonedDateTime): Slot =
    new Slot {
      override val start: Slot.Start = Slot.Start(startTime)
      override val end: Slot.End = Slot.End(endTime)
    }

}
