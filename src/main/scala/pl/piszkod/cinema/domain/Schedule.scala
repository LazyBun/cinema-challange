package pl.piszkod.cinema.domain

// TODO: This could be separate entites, but for now I've decided to use a simple typealiased map
type Schedule = Map[Room.Name, Seq[Slot]]
type RoomSchedule = Seq[Slot]

