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

package be.cetic.tsimulus.generators.composite

import be.cetic.tsimulus.config.{GeneratorFormat, Model}
import be.cetic.tsimulus.generators.{Generator, TimeToJson}
import be.cetic.tsimulus.timeseries.TimeSeries
import be.cetic.tsimulus.timeseries.composite.TransitionTimeSeries
import org.joda.time.{Duration, LocalDateTime}
import spray.json.{JsObject, JsString, JsValue, _}

/**
  * A transition generator, similar to [[TransitionGenerator]], but appropriate for generating binary time series.
  */
class BinaryTransitionGenerator(name: Option[String],
                          val first: Either[String, Generator[Any]],
                          val second: Either[String, Generator[Any]],
                          val time: LocalDateTime) extends Generator[Boolean](name, "binary-transition")
{
   override def timeseries(generators: (String) => Generator[Any]) =
   {
      val firstBase = Model.generator(generators)(first).timeseries(generators) match {
         case t: TimeSeries[Boolean] => t
      }

      val secondBase = Model.generator(generators)(second).timeseries(generators) match {
         case t: TimeSeries[Boolean] => t
      }

      TransitionTimeSeries[Boolean](firstBase, secondBase, time, None)
   }

   override def toString = "BinaryTransitionGenerator(" + name + "," + first + "," + second + "," + time + ")"

   override def equals(o: Any) = o match {
      case that: BinaryTransitionGenerator => that.name == this.name &&
         that.first == this.first &&
         that.second == this.second &&
         that.time == this.time
      case _ => false
   }

   override def toJson: JsValue = {
      val _first = (first match {
         case Left(s) => s.toJson
         case Right(g) => g.toJson
      }).toJson
      val _second = (second match {
         case Left(s) => s.toJson
         case Right(g) => g.toJson
      }).toJson

      var t = Map(
         "type" -> `type`.toJson,
         "first" -> _first,
         "second" -> _second,
         "time" -> time.toJson
      )

      if(name.isDefined)
         t = t.updated("name", name.get.toJson)

      new JsObject(t)
   }
}

object BinaryTransitionGenerator extends TimeToJson
{
   def apply(value: JsValue): BinaryTransitionGenerator = {
      val fields = value.asJsObject.fields

      val name = fields.get("name").map
      {
         case JsString(x) => x
      }

      val first = fields("first") match {
         case JsString(s) => Left(s)
         case g => Right(GeneratorFormat.read(g))
      }

      val second = fields("second") match {
         case JsString(s) => Left(s)
         case g => Right(GeneratorFormat.read(g))
      }

      val time = fields("time").convertTo[LocalDateTime]

      new BinaryTransitionGenerator(name, first, second, time)
   }
}
