package main

import akka.actor.ActorSystem
import akka.util.ByteString
import akka.stream.{FlowShape, ActorMaterializer, ActorAttributes}
import akka.stream.io._
import java.io.{InputStream, ByteArrayInputStream}
import akka.stream.scaladsl._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object StreamProgram {

  val sayFlow: Flow[String, String, Unit] =
    Flow[String].map { s =>
      s + "."
    }

  val shoutFlow: Flow[String, String, Unit] =
    Flow[String].map { s =>
      s + "!!!!"
    }

  val sayAndShoutFlow: Flow[String, String, Unit] =
    Flow.fromGraph(
      GraphDSL.create() { implicit b =>
        import GraphDSL.Implicits._

        val broadcast = b.add(Broadcast[String](2))
        val merge = b.add(Merge[String](2))

        broadcast ~> sayFlow ~> merge
        broadcast ~> shoutFlow ~> merge
        FlowShape(broadcast.in, merge.out)
      }
    )

  def run(): Unit = {
    implicit lazy val system = ActorSystem("example")
    implicit val materializer = ActorMaterializer()
    Source(List("Hello World"))
      .via(sayAndShoutFlow)
      .runWith(Sink.foreach(println))
      .onComplete {
        case _ => system.shutdown()
      }
  }
}

object ConflateComparison {

  val fastStage: Flow[Unit, Unit, Unit] =
    Flow[Unit].map(_ => println("this is the fast stage"))

  val slowStage: Flow[Unit, Unit, Unit] =
    Flow[Unit].map { _ =>
      Thread.sleep(1000L)
      println("this is the SLOWWWW stage")
    }

  val conflateFlow: Flow[Unit, Unit, Unit] =
    Flow[Unit].conflate(_ => List(()))((l, u) => u :: l)
              .mapConcat(identity)

  val withConflate: Source[Unit, Unit] =
    Source(List.fill(10)(()))
      .via(fastStage)
      .via(conflateFlow)
      .via(slowStage)

  val withoutConflate: Source[Unit, Unit] =
    Source(List.fill(10)(()))
      .via(fastStage)
      .via(slowStage)

  def run(s: Source[Unit, Unit]): Unit = {
    implicit lazy val system = ActorSystem("example")
    implicit val materializer = ActorMaterializer()
    s.runWith(Sink.ignore)
      .onComplete { _ => system.shutdown() }
  }
}

object StreamFile {

  def run(): Unit = {
    implicit lazy val system = ActorSystem("example")
    implicit val materializer = ActorMaterializer()
    val is = new ByteArrayInputStream("hello\nworld".getBytes)
    StreamConverters.fromInputStream(() => is)
      .withAttributes(ActorAttributes.dispatcher("akka.stream.default-file-io-dispatcher"))
      .via(Framing.delimiter(
        ByteString("\n"), maximumFrameLength = Int.MaxValue,
        allowTruncation = true))
      .map(_.utf8String)
      .runWith(Sink.foreach(println))
      .onComplete(_ => system.shutdown())
  }
}

object AckedFileStream {

  class QueueMessage(str: String) {
    def ack(): Unit = println("acked")
    lazy val stream = new ByteArrayInputStream(str.getBytes)
  }

  sealed trait Element
  case class StringElement(s: String) extends Element
  case class QueueElement(qm: QueueMessage) extends Element

  def fileStream(m: QueueMessage): Source[StringElement, Future[Long]] =
    StreamConverters.fromInputStream(() => m.stream)
      .via(Framing.delimiter(
        ByteString("\n"), maximumFrameLength = Int.MaxValue,
        allowTruncation = true))
      .map(s => StringElement(s.utf8String))

  def run(ls: List[String]): Unit = {
    implicit lazy val system = ActorSystem("example")
    implicit val materializer = ActorMaterializer()
    Source(ls.map(s => new QueueMessage(s)))
      .map { qm =>
          fileStream(qm)
          .concat(Source(List(QueueElement(qm))))
      }.flatMapConcat(identity)
      .runWith(Sink.foreach { (r: Element) => r match {
        case QueueElement(qm) => qm.ack()
        case StringElement(s) => println(s)
      }})
      .onComplete(_ => system.shutdown())
  }
}
