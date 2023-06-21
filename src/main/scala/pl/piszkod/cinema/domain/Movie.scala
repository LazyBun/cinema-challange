package pl.piszkod.cinema.domain

import scala.concurrent.duration.Duration

case class Movie(
    title: Movie.Title,
    length: Movie.Length,
    kind: Movie.Kind
)

object Movie {

  case class Title(value: String) extends AnyVal

  case class Length(value: Duration) extends AnyVal

  sealed trait Kind

  case object Regular extends Kind

  case object Premiere extends Kind

}
