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

package be.cetic.tsimulus.generators.primary.dt

import be.cetic.tsimulus.generators.{Generator, TimeToJson}
import be.cetic.tsimulus.timeseries.primary.dt.WeekTimeSeries
import spray.json.{DefaultJsonProtocol, JsObject, JsString, JsValue, _}

/**
  * A generator for [[be.cetic.tsimulus.timeseries.primary.dt.WeekTimeSeries]].
  *
  * @param name The generator name.
  */
class WeekGenerator(name: Option[String]) extends Generator[Int](name, "week")
{
   override def timeseries(generators: String => Generator[Any]) = WeekTimeSeries()

   override def toString = "WeekGenerator()"

   override def equals(o: Any) = o match {
      case that: WeekGenerator => that.name == this.name
      case _ => false
   }

   override def toJson: JsValue = {

      val t = Map(
         "type" -> `type`.toJson
      )

      new JsObject(
         name.map(n => t + ("name" -> n.toJson)).getOrElse(t)
      )
   }
}

object WeekGenerator extends DefaultJsonProtocol with TimeToJson
{
   def apply(json: JsValue): WeekGenerator = {

      val fields = json.asJsObject.fields
      val name = fields.get("name") .map(f => f match {
         case JsString(x) => x
      })

      new WeekGenerator(name)
   }
}

