package pl.piszkod.cinema.domain

import java.time.Duration

case class Movie(
    uid: Movie.Uid,
    title: Movie.Title,
    length: Movie.Length,
    kind: Movie.Kind,
    requirements: Seq[Movie.Requirement]
)

object Movie {

  case class Uid(value: String) extends AnyVal

  case class Title(value: String) extends AnyVal

  case class Length(value: Duration) extends AnyVal

  sealed trait Kind

  object Kind {
    case object Regular extends Kind

    case object Premiere extends Kind
  }

  sealed trait Requirement

  object Requirement {
    case object Glasses3D extends Requirement
  }

}
