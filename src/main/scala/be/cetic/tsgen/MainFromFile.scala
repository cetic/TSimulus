package be.cetic.tsgen

import java.io.File

import be.cetic.tsgen.config.Configuration
import com.github.nscala_time.time.Imports._
import be.cetic.tsgen.config.GeneratorLeafFormat._

import scala.io.Source
import spray.json._

object MainFromFile
{
   def main(args: Array[String])
   {
      val content = Source .fromFile(new File(args(0)))
                           .getLines()
                           .mkString("\n")

      val dtf = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss.SSS")

      val document = content.parseJson

      val config = document.convertTo[Configuration]

      println("date;series;value")

      Main.generate(Main.config2Results(config)) foreach (e => println(dtf.print(e._1) + ";" + e._2 + ";" + e._3))
   }
}
