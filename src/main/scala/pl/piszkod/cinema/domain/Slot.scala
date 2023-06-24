package pl.piszkod.cinema.domain

import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import java.time.Duration

// TODO: Document the idea behind the slot type
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
    // TODO: Better name? This is to indicate that those values are a copy (because we want to have historical data like that)
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
