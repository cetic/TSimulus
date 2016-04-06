package be.cetic.tsgen.config

import java.io.File

import com.github.nscala_time.time.Imports._
import spray.json._
import DefaultJsonProtocol._

import scala.io.Source


import be.cetic.tsgen.config.GeneratorLeafFormat._

/**
  * An importer of a configuration from a JSON document.
  */
object ConfigurationImporter
{
   val dtf = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss")


   def main(args: Array[String])
   {
      val file = new File("/Users/mg/Documents/IDEA projects/time-series-generator/examples/demo1.json")

      // val document = Source.fromFile(file).getLines().mkString("").parseJson

      val document =
         """
           |{
           |    "name": "daily-generator",
           |    "type": "daily",
           |    "points": {
           |      "11:00:00.000" : 6,
           |      "17:00:00.000" : 8,
           |      "07:00:00.000": 2
           |      }
           |}
         """.stripMargin.parseJson


      println(document.convertTo[DailyGenerator])
      println("---")
      println(document.convertTo[DailyGenerator].toJson)
   }
}
