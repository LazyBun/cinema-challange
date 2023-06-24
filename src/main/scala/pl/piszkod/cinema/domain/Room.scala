package pl.piszkod.cinema.domain

import java.time.Duration

case class Room(
    name: Room.Name,
    cleaningTime: Room.CleaningTime,
    capabilities: Seq[Room.Capability]
)

object Room {

  // Assuming room name to be unique
  case class Name(value: String) extends AnyVal

  case class CleaningTime(value: Duration)

  sealed trait Capability

  object Capability {

    case object ThreeD extends Capability

  }

}
