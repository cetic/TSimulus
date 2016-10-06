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

package be.cetic.rtsgen.generators.primary

import be.cetic.rtsgen.generators.Generator
import be.cetic.rtsgen.timeseries.primary.YearlyTimeSeries
import spray.json._

/**
  * A generator for [[be.cetic.rtsgen.timeseries.primary.YearlyTimeSeries]].
  */
class YearlyGenerator(name: Option[String],
                      val points: Map[Int, Double]) extends Generator[Double](name, "yearly")
{
   override def timeseries(generators: String => Generator[Any]) = YearlyTimeSeries(points)

   override def toString = "YearlyGenerator(" + name + "," + points + ")"

   override def equals(o: Any) = o match {
      case that: YearlyGenerator => that.name == this.name && that.points == this.points
      case _ => false
   }

   override def toJson: JsValue = {

      val t = Map(
         "type" -> `type`.toJson,
         "points" -> points.map { case (y,v) => (y.toString, v)}.toJson
      )

      new JsObject(
         name.map(n => t + ("name" -> n.toJson)).getOrElse(t)
      )
   }
}

object YearlyGenerator extends DefaultJsonProtocol {

   def apply(json: JsValue): YearlyGenerator = {
      val name = json.asJsObject.fields.get("name").map
      {
         case JsString(x) => x
      }

      val points = json.asJsObject.fields("points") match {
         case JsObject(x) => x
         case _ => throw new ClassCastException
      }

      val r = points map { case (k,v) => (k.toInt, v match { case JsNumber(x) => x.toDouble })}

      new YearlyGenerator(name, r)
   }
}
