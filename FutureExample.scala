package main

import scalaz.effect.IO
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object FutureProgram {

  def hello: Future[IO[Unit]] =
    Future { IO { println("Hello World!") } }

  def helloAgain: Future[IO[Unit]] =
    Future { IO { println("Hello Again!") } }

  def composed: Future[IO[Unit]] =
    (for {
      f <- hello
      f2 <- helloAgain
      x = for {
        _ <- f
        _ <- f2
      } yield ()
    } yield x)

  def run(): Future[Unit] =
    composed.map(_.unsafePerformIO)
}
