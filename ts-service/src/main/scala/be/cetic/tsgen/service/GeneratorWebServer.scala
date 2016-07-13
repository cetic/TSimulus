package be.cetic.tsgen.service

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.ByteString

import scala.io.StdIn
import be.cetic.tsgen.core.config.Configuration
import spray.json._
import be.cetic.tsgen.core.config.GeneratorLeafFormat._
import org.joda.time.format.DateTimeFormat
import com.github.nscala_time.time.Imports._


case class Config(port: Int = 8080, host: String = "localhost")


/**
  * /generator => All the values of a call to the generator with a configuration document provided in the POST parameter
  * /generator/date => The values of all data for the greatest date before or equal to the specified one. format: yyyy-MM-dd'T'HH:mm:ss.SSS
  * /generator/d1/d2 => The values for the dates between d1 (excluded) and d2 (included)
  */
object GeneratorWebServer {

   private val dtf = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")

   def main(args: Array[String]) : Unit =
   {

      val parser = new scopt.OptionParser[Config]("ts-gen-service") {
         head("ts-gen-service", "0.0.1")

         opt[Int]('p', "port").action( (x, c) =>
            c.copy(port = x) )
                .text("The port the service must listen to.")

         opt[String]('h', "host")
            .action( (x, c) => c.copy(host = x) )
            .text("The host on which the service is running.")
      }

      if(parser.parse(args, Config()).isEmpty) System.exit(1)
      val config = parser.parse(args, Config()).get

      implicit val system = ActorSystem("tsgen-system")
      implicit val materializer = ActorMaterializer()
      implicit val executionContext = system.dispatcher

      val fullRoute = path("generator")
      {
         post
         {
            decodeRequest
            {
               entity(as[String])
               { document =>

                  val config = document.parseJson.convertTo[Configuration]

                  val results = be.cetic.tsgen.core.Main.generate(be.cetic.tsgen.core.Main.config2Results(config))

                  val answer = Source(results.map(x => dtf.print(x._1) + ";" + x._2 + ";" + x._3))

                  complete(
                     HttpEntity(
                        ContentTypes.`text/csv(UTF-8)`,
                        answer.map(a => ByteString(s"$a\n"))
                     )
                  )
               }
            }
         }
      }

      val lastRoute = path("generator" / PathMatchers.Segments)
      {
         segments =>
         post
         {
            decodeRequest
            {
               entity(as[String])
               { document =>

                  val config = document.parseJson.convertTo[Configuration]
                  val results = be.cetic.tsgen.core.Main.generate(be.cetic.tsgen.core.Main.config2Results(config))

                  val answer = segments match {

                     case List(limit) => {
                        // We are looking for the values corresponding to a date before or equal to limit
                        val reference = LocalDateTime.parse(limit, dtf)
                        val last = scala.collection.mutable.Map[String, (LocalDateTime, String)]()

                        results  .takeWhile(entry => entry._1 <= reference)
                                 .foreach(entry =>
                                 {
                                    val date = entry._1
                                    val data = entry._2
                                    val value = entry._3.toString

                                    last.put(data, (date, value))
                                 })

                        Source(last.toMap.map(entry => dtf.print(entry._2._1) + ";" + entry._1 + ";" + entry._2._2))
                     }

                     case List(start, stop) => {
                        val startDate = LocalDateTime.parse(start, dtf)
                        val endDate = LocalDateTime.parse(stop, dtf)

                        val validValues = results.dropWhile(entry => entry._1 <= startDate)
                                                 .takeWhile(entry => entry._1 <= endDate)
                                                 .map(x => dtf.print(x._1) + ";" + x._2 + ";" + x._3.toString)
                        Source(validValues)
                     }

                     case _ => Source(List("invalid segments: " + segments.mkString("/")))
                  }

                  complete(
                     HttpEntity(
                        ContentTypes.`text/csv(UTF-8)`,
                        answer.map(a => ByteString(s"$a\n"))
                     )
                  )
               }
            }
         }
      }

      val route =  lastRoute ~ fullRoute

      val bindingFuture = Http().bindAndHandle(route, config.host, config.port)

      println(s"Server online at http://${config.host}:${config.port}/\nPress RETURN to stop...")
      StdIn.readLine() // let it run until user presses return
      bindingFuture
         .flatMap(_.unbind()) // trigger unbinding from the port
         .onComplete(_ => system.terminate()) // and shutdown when done
   }
}