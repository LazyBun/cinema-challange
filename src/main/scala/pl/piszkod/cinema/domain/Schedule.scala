package pl.piszkod.cinema.domain

// TODO: This could be defined as separate entity, but for simplicity's sake, I decided to just typealias map
type Schedule = Map[Room.Name, Seq[Slot]]
type RoomSchedule = Seq[Slot]

