package pl.piszkod.cinema.service

import cats.effect.IO
import pl.piszkod.cinema.domain.Movie

trait MovieCatalog {

  def find(uid: Movie.Uid): IO[Option[Movie]]

}

class InMemoryMovieCatalog(val catalog: Seq[Movie]) extends MovieCatalog {

  override def find(uid: Movie.Uid): IO[Option[Movie]] = ???

}
