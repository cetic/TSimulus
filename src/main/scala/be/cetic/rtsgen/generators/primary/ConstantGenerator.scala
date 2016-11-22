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
import be.cetic.rtsgen.timeseries.primary.ConstantTimeSeries
import spray.json.{JsNumber, JsString, JsValue, _}

/**
  * A generator for [[be.cetic.rtsgen.timeseries.primary.ConstantTimeSeries]].
  *
  * The generated time series always produces the same value.
  */
class ConstantGenerator(name: Option[String],
                        val value: Double) extends Generator[Double](name, "constant")
{
   override def timeseries(generators: String => Generator[Any]) = ConstantTimeSeries(value)

   override def toString = "Constant(" + name + ", " + value + ")"

   override def equals(o: Any) = o match {
      case that: ConstantGenerator => that.name == this.name && that.value == this.value
      case _ => false
   }

   override def toJson: JsValue = {
      val t = Map(
         "type" -> `type`.toJson,
         "value" -> value.toJson
      )

      new JsObject(
         name.map(n => t + ("name" -> n.toJson)).getOrElse(t)
      )
   }
}

object ConstantGenerator
{
   def apply(json: JsValue): ConstantGenerator = {

      val fields = json.asJsObject.fields
      val name = fields.get("name").map
      {
         case JsString(x) => x
      }

      val value = fields("value") match {
         case JsNumber(n) => n.toDouble
      }

      new ConstantGenerator(name, value)
   }
}
