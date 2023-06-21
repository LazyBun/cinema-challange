package pl.piszkod.cinema.domain

case class Room(name: Room.Name)

object Room {

  case class Name(value: String) extends AnyVal

}
