package main

import akka.actor.{ActorSystem, Actor, Props}

class HelloActor extends Actor {
  def receive: PartialFunction[Any, Unit] = {
    case _ => println("Hello World!")
  }
}

object ActorProgram {

  def run(): Unit = {
    val system = ActorSystem("hello-world")
    val actor = system.actorOf(Props[HelloActor], name = "helloactor")
    actor ! "hello"
    system.shutdown()
  }
}
