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

package be.cetic.rtsgen.generators.binary

import be.cetic.rtsgen.config.{GeneratorFormat, Model}
import be.cetic.rtsgen.generators.Generator
import be.cetic.rtsgen.timeseries.TimeSeries
import be.cetic.rtsgen.timeseries.binary.NotTimeSeries
import spray.json.{JsObject, JsString, JsValue, _}

/**
  * A generator for [[be.cetic.rtsgen.timeseries.binary.NotTimeSeries]].
  */
class NotGenerator(name: Option[String],
                   val generator: Either[String, Generator[Any]]) extends Generator[Any](name, "or")
{
   override def timeseries(generators: (String) => Generator[Any]) =
   {
      val base = Model.generator(generators)(generator).timeseries(generators) match {
         case t: TimeSeries[Boolean] => t
      }

      NotTimeSeries(base)
   }

   override def toString = "Not(" + name + ", " + generator + ")"

   override def equals(o: Any) = o match {
      case that: NotGenerator => that.name == this.name &&
         that.generator == this.generator
      case _ => false
   }

   override def toJson: JsValue = {
      val _generator = generator match
      {
         case Left(s) => s.toJson
         case Right(g) => g.toJson
      }

      var t = Map(
         "generator" -> _generator,
         "type" -> `type`.toJson
      )

      if(name.isDefined) t = t.updated("name", name.toJson)

      new JsObject(t)
   }
}

object NotGenerator extends DefaultJsonProtocol
{
   def apply(value: JsValue): NotGenerator = {
      val fields = value.asJsObject.fields

      val name = fields.get("name").map(_.convertTo[String])

      val generator = fields("generator") match {
         case JsString(s) => Left(s)
         case g => Right(GeneratorFormat.read(g))
      }

      new NotGenerator(name, generator)
   }
}
