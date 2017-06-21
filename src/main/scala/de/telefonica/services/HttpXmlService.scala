package de.telefonica.services

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import scala.io.StdIn

/**
  * Created by denniskleine on 10.05.17.
  */
object HttpXmlService extends XmlService {

  // needed to run the route
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  // needed for the future map/flatmap in the end
  implicit val executionContext = system.dispatcher

  val logger = Logging(system, getClass)

  def main(args: Array[String]): Unit = {
    if (args.length < 2) {
      System.err.println("Usage: HttpXmlService <host> <port> ")
      System.exit(1)
    }

    val Array(host, port) = args

    val bindingFuture = Http().bindAndHandle(route, host, port.toInt)
    println(s"Server online at http://$host:$port/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
    }
}
