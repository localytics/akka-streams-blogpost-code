package main

import akka.actor.{ActorSystem, Actor, Props}

class HelloActor extends Actor {
  def receive: PartialFunction[Any, Unit] = {
    case s: String => println(s + "!")
    case _ => println("what am I even supposed to do here?")
  }
}

object ActorProgram {

  def run(): Unit = {
    val system = ActorSystem("hello-world")
    val actor = system.actorOf(Props[HelloActor], name = "helloactor")
    actor ! "Hello World"
    system.shutdown()
  }
}
