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


import java.security.InvalidParameterException

import be.cetic.tsimulus.generators.Generator
import be.cetic.tsimulus.timeseries.primary.WeeklyTimeSeries
import org.joda.time.DateTimeConstants
import spray.json.{JsNumber, JsObject, JsString, JsValue, _}

/**
  * A generator for [[be.cetic.tsimulus.timeseries.primary.WeeklyTimeSeries]].
  */
class WeeklyGenerator(name: Option[String],
                      val points: Map[String, Double]) extends Generator[Double](name, "weekly")
{
   override def timeseries(generators: String => Generator[Any]) =
   {
      def day = (s: String) => s match {
         case "monday" => DateTimeConstants.MONDAY
         case "tuesday" => DateTimeConstants.TUESDAY
         case "wednesday" => DateTimeConstants.WEDNESDAY
         case "thursday" => DateTimeConstants.THURSDAY
         case "friday" => DateTimeConstants.FRIDAY
         case "saturday" => DateTimeConstants.SATURDAY
         case "sunday" => DateTimeConstants.SUNDAY
         case _ => throw new InvalidParameterException(s"'${s}' is not a valid day name.")
      }

      WeeklyTimeSeries(points map {case (k,v) => (day(k), v)})
   }

   override def toString = "WeeklyGenerator(" + name + "," + points + ")"

   override def equals(o: Any) = o match {
      case that: WeeklyGenerator => that.name == this.name && that.points == this.points
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

object WeeklyGenerator
{
   def apply(value: JsValue): WeeklyGenerator = {
      val fields = value.asJsObject.fields

      val name = fields.get("name").map
      {
         case JsString(x) => x
      }

      val points = value.asJsObject.fields("points") match {
         case JsObject(x) => x
         case _ => throw new ClassCastException
      }

      val r = points map { case (k,v) => (k, v match { case JsNumber(x) => x.toDouble })}

      val validDayNames = List("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")
      val unmatchingDayNames = r.keySet.filterNot(validDayNames contains _)
      if(!unmatchingDayNames.isEmpty) throw new InvalidParameterException("The following day names are not valid: " + unmatchingDayNames)

      new WeeklyGenerator(name, r)
   }
}
