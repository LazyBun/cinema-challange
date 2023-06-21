package pl.piszkod.cinema.domain

import java.time.ZonedDateTime
import scala.concurrent.duration.Duration

sealed trait Slot

case class Block(
    start: Block.Start,
    blockTime: Block.Time
) extends Slot

object Block {
  case class Start(value: ZonedDateTime) extends AnyVal
  case class Time(value: Duration) extends AnyVal
}

case class Show(
    movie: Movie,
    start: Show.Start,
    cleaningTime: Show.CleaningTime
) extends Slot

object Show {

  // FIXME consider if this is the time representation we want to go with
  case class Start(value: ZonedDateTime) extends AnyVal

  case class CleaningTime(value: Duration) extends AnyVal

}
