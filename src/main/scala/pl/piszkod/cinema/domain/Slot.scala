package pl.piszkod.cinema.domain

import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import java.time.Duration

sealed trait Slot {
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
  case class Start(value: ZonedDateTime) extends AnyVal
  case class Time(value: Duration) extends AnyVal
}

case class Show(
    override val start: Slot.Start,
    movie: Movie,
    cleaningTime: Show.CleaningTime
) extends Slot {
  override val end: Slot.End =
    Slot.End(start.value.plus(movie.length.value).plus(cleaningTime.value))
}

object Show {

  // FIXME consider if this is the time representation we want to go with
  case class Start(value: ZonedDateTime) extends AnyVal

  case class CleaningTime(value: Duration) extends AnyVal

}
