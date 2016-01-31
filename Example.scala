package main

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import akka.stream.Supervision.Decider
import akka.stream.scaladsl._

object Streams {

  implicit lazy val system = ActorSystem("example")
  val decider: Decider = Supervision.resumingDecider
  implicit val materializer =
    ActorMaterializer(ActorMaterializerSettings(system)
                        .withSupervisionStrategy(decider))

  val fast = Source.fromIterator(() => Iterator.fill(10)(()))
                .map(_ => println("with conflate"))
                .conflate(List(_)){ (l, i) => i :: l }
                .mapConcat(identity(_))

  val slow = Source.fromIterator(() => Iterator.fill(10)(()))
                .map(_ => println("without conflate"))

  val via = Flow[Unit].map { _ =>
    Thread.sleep(1000L)
    println("via")
  }
}
