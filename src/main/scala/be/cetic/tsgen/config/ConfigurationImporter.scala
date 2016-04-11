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
           |   "generators": [
           |      {
           |         "name": "daily-generator",
           |         "type": "daily",
           |         "points": {"10:00:00.000": 4, "17:00:00.000": 32, "20:00:00.000": 27}
           |      },
           |      {
           |         "name": "noisy-daily",
           |         "type": "aggregate",
           |         "aggregator": "sum",
           |         "generators": [
           |            "daily-generator",
           |            {
           |                "type": "arma",
           |                "model": { "phi": [0.5], "std": 0.25, "c": 0, "seed": 159357},
           |                "timestep": 180000
           |            }
           |         ]
           |      },
           |      {
           |         "name":  "partial-daily",
           |         "type": "partial",
           |         "generator": "daily-generator",
           |         "from": "2016-01-01 00:00:00.000",
           |         "to": "2017-01-01 00:00:00.000"
           |      }
           |   ],
           |   "series": [
           |      {
           |         "name": "series-A",
           |         "generator": "daily-generator",
           |         "frequency": 60000
           |      },
           |      {
           |         "name": "series-B",
           |         "generator": "noisy-daily",
           |         "frequency": 30000
           |      }
           |   ],
           |   "from": "2016-01-01 00:00:00.000",
           |   "to": "2016-10-01 00:00:00.000"
           |}
         """.stripMargin.parseJson

      val config = document.convertTo[Configuration]

      println(config.timeSeries)




   }
}
