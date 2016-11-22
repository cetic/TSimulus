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
import be.cetic.rtsgen.timeseries.binary.LesserThanTimeSeries
import spray.json.{DefaultJsonProtocol, JsObject, JsString, JsValue, RootJsonFormat, _}


/**
  * A generator for [[be.cetic.rtsgen.timeseries.binary.LesserThanTimeSeries]].
  */
class LesserThanGenerator( name: Option[String],
                            val a: Either[String, Generator[Any]],
                            val b: Either[String, Generator[Any]],
                            val strict: Option[Boolean]) extends Generator[Any](name, "lesser-than")
{
   override def timeseries(generators: (String) => Generator[Any]) =
   {
      val first = Model.generator(generators)(a).timeseries(generators) match {
         case t: TimeSeries[Double] => t
      }

      val second = Model.generator(generators)(b).timeseries(generators) match {
         case t: TimeSeries[Double] => t
      }

      new LesserThanTimeSeries(first, second, strict match {
         case None => true
         case Some(x) => x
      })
   }

   override def toString = "LesserThan(" + name + ", " + a + ", " + b + ", " + strict + ")"

   override def equals(o: Any) = o match {
      case that: LesserThanGenerator => that.name == this.name &&
         that.a == this.a &&
         that.b == this.b &&
         that.strict == this.strict
      case _ => false
   }

   override def toJson: JsValue = {
      val _a = a match
      {
         case Left(s) => s.toJson
         case Right(g) => g.toJson
      }

      val _b = b match
      {
         case Left(s) => s.toJson
         case Right(g) => g.toJson
      }

      var t = Map(
         "a" -> _a,
         "b" -> _b,
         "type" -> `type`.toJson
      )

      if(name.isDefined) t = t.updated("name", name.toJson)
      if(strict.isDefined) t = t.updated("strict", strict.toJson)

      new JsObject(t)
   }
}

object LesserThanGenerator extends DefaultJsonProtocol
{
   def apply(value: JsValue): LesserThanGenerator =
   {
      val fields = value.asJsObject.fields

      val name = fields.get("name").map(_.convertTo[String])
      val strict = fields.get("strict").map(_.convertTo[Boolean])

      val a = fields("a") match {
         case JsString(s) => Left(s)
         case g => Right(GeneratorFormat.read(g))
      }

      val b = fields("b") match {
         case JsString(s) => Left(s)
         case g => Right(GeneratorFormat.read(g))
      }

      new LesserThanGenerator(name, a, b, strict)
   }
}