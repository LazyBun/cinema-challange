package pl.piszkod.cinema.domain

// FIXME: Might not be a domain thing but e.g. some ScheduleService that will then contain this
case class Schedule(schedule: Map[Room, Slot])
