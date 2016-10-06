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

package be.cetic.rtsgen.generators.composite

import be.cetic.rtsgen.config.{GeneratorFormat, Model}
import be.cetic.rtsgen.generators.Generator
import be.cetic.rtsgen.timeseries.TimeSeries
import be.cetic.rtsgen.timeseries.composite.DivideTimeSeries
import spray.json.{JsObject, JsString, JsValue, _}

/**
  * A generator for [[be.cetic.rtsgen.timeseries.composite.DivideTimeSeries]].
  */
class DivideGenerator(name: Option[String],
                      val numerator: Either[String, Generator[Any]],
                      val denominator: Either[String, Generator[Any]]) extends Generator[Double](name, "divide")
{
   override def timeseries(gen: String => Generator[Any]) =
   {
      val num = Model.generator(gen)(numerator).timeseries(gen) match {
         case t: TimeSeries[Double] => t
      }

      val den = Model.generator(gen)(denominator).timeseries(gen) match {
         case t: TimeSeries[Double] => t
      }


      new DivideTimeSeries(num, den)
   }

   override def toString = "DivideGenerator(" + name + "," + numerator + "," + denominator + ")"

   override def equals(o: Any) = o match {
      case that: DivideGenerator => that.name == this.name && that.numerator == this.numerator && that.denominator == this.denominator
      case _ => false
   }

   override def toJson: JsValue = {
      val _numerator = (numerator match {
         case Left(s) => s.toJson
         case Right(x) => x.toJson
      }).toJson

      val _denominator = (denominator match {
         case Left(s) => s.toJson
         case Right(x) => x.toJson
      }).toJson

      val t = Map(
         "type" -> `type`.toJson,
         "numerator" -> _numerator,
         "denominator" -> _denominator
      )

      new JsObject(
         name.map(n => t + ("name" -> n.toJson)).getOrElse(t)
      )
   }
}

object DivideGenerator
{
   def apply(value: JsValue): DivideGenerator = {
      val fields = value.asJsObject.fields

      val name = fields.get("name").map
      {
         case JsString(x) => x
      }

      val numerator = fields("numerator") match {
         case JsString(s) => Left(s)
         case g => Right(GeneratorFormat.read(g))
      }

      val denominator = fields("denominator") match {
         case JsString(s) => Left(s)
         case g => Right(GeneratorFormat.read(g))
      }

      new DivideGenerator(name, numerator, denominator)
   }
}
