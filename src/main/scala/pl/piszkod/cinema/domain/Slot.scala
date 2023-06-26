package pl.piszkod.cinema.domain

import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import java.time.Duration

trait Slot {
  val start: Slot.Start

  val end: Slot.End

  def collides(otherSlot: Slot): Boolean = {
    start.value.isBefore(otherSlot.end.value) && otherSlot.start.value.isBefore(
      end.value
    )
  }

}

object Slot {

  case class Start(value: ZonedDateTime) extends AnyVal
  case class End(value: ZonedDateTime) extends AnyVal

}

case class Block(
    override val start: Slot.Start,
    blockTime: Block.Time
) extends Slot {

  override val end: Slot.End = Slot.End(start.value.plus(blockTime.value))

}

object Block {
  case class Time(value: Duration) extends AnyVal
}

case class Show(
    override val start: Slot.Start,
    // TODO: Snapshot _might_ not be the best name here, but the idea is that future changes of the movie/cleaning time should not affect already schedule items
    movieSnapshot: Movie,
    roomCleaningTimeSnapshot: Show.CleaningTime
) extends Slot {
  override val end: Slot.End =
    Slot.End(
      start.value
        .plus(movieSnapshot.length.value)
        .plus(roomCleaningTimeSnapshot.value)
    )
}

object Show {

  case class CleaningTime(value: Duration) extends AnyVal

}
