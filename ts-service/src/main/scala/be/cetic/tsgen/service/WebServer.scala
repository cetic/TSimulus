package be.cetic.tsgen.service

import akka.actor.ActorSystem
import akka.http.javadsl.server.Route
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.ByteString

import scala.io.{Source, StdIn}
import be.cetic.tsgen.core.config.Configuration
import spray.json._
import be.cetic.tsgen.core.config.GeneratorLeafFormat._

object WebServer {

   private val PORT = 8080

   def main(args: Array[String]) {

      implicit val system = ActorSystem("tsgen-system")
      implicit val materializer = ActorMaterializer()
      // needed for the future flatMap/onComplete in the end
      implicit val executionContext = system.dispatcher

      def innerRoute() =
           post {
              // decompress gzipped or deflated requests if required
              decodeRequest {

                 entity(as[String]) { document =>

                    val config = document.parseJson.convertTo[Configuration]

                    val results = be.cetic.tsgen.core.Main.generate(be.cetic.tsgen.core.Main.config2Results(config))

                    complete(
                       HttpEntity(
                          ContentTypes.`text/plain(UTF-8)`,
                          results.map(x => x._1 + ";" + x._2 + ";" + x._3).mkString("\n") // TODO: make it a stream. cf http://doc.akka.io/docs/akka/2.4.7/scala/http/introduction.html#using-akka-http
                       )
                    )
                 }
              }


            }

      val route = path("generator") { innerRoute() }


      val bindingFuture = Http().bindAndHandle(route, "localhost", PORT)

      println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
      StdIn.readLine() // let it run until user presses return
      bindingFuture
         .flatMap(_.unbind()) // trigger unbinding from the port
         .onComplete(_ => system.terminate()) // and shutdown when done
   }
}