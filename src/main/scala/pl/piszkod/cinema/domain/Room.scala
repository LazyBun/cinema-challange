package pl.piszkod.cinema.domain

case class Room(name: Room.Name)

object Room {

  // Assuming room name to be unique
  case class Name(value: String) extends AnyVal

}
