/*
 * Copyright 2106 Cetic ASBL
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package be.cetic.tsimulus.generators.primary

import be.cetic.tsimulus.generators.Generator
import be.cetic.tsimulus.timeseries.primary.DailyTimeSeries
import com.github.nscala_time.time.Imports._
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormatterBuilder
import spray.json._

/**
  * A generator for [[be.cetic.tsimulus.timeseries.primary.DailyTimeSeries]].
  */
class DailyGenerator(name: Option[String],
                     val points: Map[LocalTime, Double]) extends Generator[Double](name, "daily")
{
   override def timeseries(generators: String => Generator[Any]) = DailyTimeSeries(points)

   override def toString = "DailyGenerator(" + name + "," + points + ")"

   override def equals(o: Any) = o match {
      case that: DailyGenerator => that.name == this.name && that.points == this.points
      case _ => false
   }

   override def toJson: JsValue = {
      val t = Map(
         "type" -> `type`.toJson,
         "points" -> points.toJson
      )

      new JsObject(
         name.map(n => t + ("name" -> n.toJson)).getOrElse(t)
      )
   }
}

object DailyGenerator extends DefaultJsonProtocol {
   private val ttf = {
      val parsers = Array(
         DateTimeFormat.forPattern("HH:mm:ss.SSS").getParser,
         DateTimeFormat.forPattern("HH:mm:ss").getParser
      )

      new DateTimeFormatterBuilder().append(null, parsers).toFormatter()
   }

   def apply(value: JsValue) =
   {
      val fields = value.asJsObject.fields

      val name = fields.get("name").map
      {
         case JsString(x) => x
      }

      val points = fields("points") match {
         case JsObject(x) => x
         case _ => throw new ClassCastException
      }

      val r = points map { case (k,v) => (ttf.parseLocalTime(k), v match { case JsNumber(x) => x.toDouble })}

      new DailyGenerator(name, r)
   }
}
