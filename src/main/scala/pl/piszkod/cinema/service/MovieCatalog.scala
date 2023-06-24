package pl.piszkod.cinema.service

import cats.data.EitherT
import cats.effect.IO
import pl.piszkod.cinema.domain.Movie
import pl.piszkod.cinema.service

// Assumption: In real implementation this would be more intricate, and so should not be treated as simple repository
trait MovieCatalog {

  def get(uid: Movie.Uid): EitherT[IO, MovieCatalog.Error, Movie]

}

object MovieCatalog {
  sealed trait Error

  object Error {

    case class MovieNotFound(uid: Movie.Uid) extends service.MovieCatalog.Error

  }
}

class InMemoryMovieCatalog(val catalog: Map[Movie.Uid, Movie])
    extends MovieCatalog {
  override def get(uid: Movie.Uid): EitherT[IO, MovieCatalog.Error, Movie] =
    EitherT.fromOption[IO](
      catalog.get(uid),
      MovieCatalog.Error.MovieNotFound(uid)
    )
}
