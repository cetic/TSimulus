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
import be.cetic.rtsgen.timeseries.composite.ConditionalTimeSeries
import be.cetic.rtsgen.timeseries.missing.UndefinedTimeSeries
import spray.json.{JsObject, JsString, JsValue, _}


/**
  * A generator for [[be.cetic.rtsgen.timeseries.composite.ConditionalTimeSeries]].
  *
  * The generated values come from a time series if the tested value of a binary time series is true, and come
  * from an other time series otherwise.
  */
class ConditionalGenerator(name: Option[String],
                           val condition: Either[String, Generator[Any]],
                           val success: Either[String, Generator[Any]],
                           val failure: Option[Either[String, Generator[Any]]]) extends Generator[Any](name, "conditional")
{

   override def timeseries(generators: (String) => Generator[Any]) =
   {
      val cond = Model.generator(generators)(condition).timeseries(generators) match {
         case t: TimeSeries[Boolean] => t
      }

      val a = Model.generator(generators)(success).timeseries(generators) match {
         case t: TimeSeries[Any] => t
      }

      val b = failure.map(f => Model.generator(generators)(f).timeseries(generators) match {
         case t: TimeSeries[Any] => t
      }).getOrElse(new UndefinedTimeSeries())

      ConditionalTimeSeries(cond, a, b)
   }

   override def toString = "ConditionalGenerator(" + name + "," + condition + "," + success + "," + failure + ")"

   override def equals(o: Any) = o match {
      case that: ConditionalGenerator =>  that.name == this.name &&
                                          that.condition == this.condition &&
                                          that.success == this.success &&
                                          that.failure == this.failure
      case _ => false
   }

   override def toJson: JsValue = {
      val _condition = (condition match {
         case Left(s) => s.toJson
         case Right(g) => g.toJson
      }).toJson

      val _success = (success match {
         case Left(s) => s.toJson
         case Right(g) => g.toJson
      }).toJson

      var t = Map(
         "type" -> `type`.toJson,
         "condition" -> _condition,
         "success" -> _success
      )

      if(failure.isDefined)
      {
         val _failure = (failure.get match {
            case Left(s) => s.toJson
            case Right(g) => g.toJson
         }).toJson

         t = t.updated("failure", _failure)
      }

      new JsObject(
         name.map(n => t + ("name" -> n.toJson)).getOrElse(t)
      )
   }
}

object ConditionalGenerator
{
   def apply(value: JsValue): ConditionalGenerator = {
      val fields = value.asJsObject.fields

      val name = fields.get("name").map
      {
         case JsString(x) => x
      }

      val condition = fields("condition") match {
         case JsString(s) => Left(s)
         case g => Right(GeneratorFormat.read(g))
      }

      val success = fields("success") match {
         case JsString(s) => Left(s)
         case g => Right(GeneratorFormat.read(g))
      }

      val failure = if(fields.contains("failure")) fields("failure") match {
         case JsString(s) => Some(Left(s))
         case g => Some(Right(GeneratorFormat.read(g)))
      }
                    else None

      new ConditionalGenerator(name, condition, success, failure)
   }
}
