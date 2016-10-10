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

package be.cetic.rtsgen.generators.missing

import be.cetic.rtsgen.config.{GeneratorFormat, Model}
import be.cetic.rtsgen.generators.{Generator, TimeToJson}
import be.cetic.rtsgen.timeseries.TimeSeries
import be.cetic.rtsgen.timeseries.missing.DefaultTimeSeries
import spray.json.{JsArray, JsObject, JsString, JsValue, _}

/**
  * A generator for [[be.cetic.rtsgen.timeseries.missing.DefaultTimeSeries]].
  */
class DefaultGenerator(name: Option[String], val gens: Seq[Either[String, Generator[Any]]]) extends Generator[Any](name, "first-of")
{
   override def timeseries(generators: (String) => Generator[Any]) =
   {
      val underlyings = gens.map(g => Model.generator(generators)(g).timeseries(generators) match {
         case t: TimeSeries[Any] => t
      })

      DefaultTimeSeries(underlyings)
   }

   override def toString = "UndefinedGenerator(" + name + "," + gens + ")"

   override def equals(o: Any) = o match {
      case that: DefaultGenerator => that.gens == this.gens
      case _ => false
   }

   override def toJson: JsValue =
   {
      val t = Map(
         "type" -> `type`.toJson,
         "generators" -> gens.map(either2json).toJson
      )

      new JsObject(
         name.map(n => t + ("name" -> n.toJson)).getOrElse(t)
      )
   }
}

object DefaultGenerator
{
   def apply(value: JsValue): DefaultGenerator = {
      val fields = value.asJsObject.fields

      val name = fields.get("name").map
      {
         case JsString(x) => x
      }

      val generators = fields("generators") match {
         case JsArray(l) => l.map
         {
            case JsString(s) => Left(s)
            case g => Right(GeneratorFormat.read(g))
         }
      }

      new DefaultGenerator(name, generators)
   }
}