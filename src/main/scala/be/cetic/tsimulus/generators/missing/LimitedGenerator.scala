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

package be.cetic.tsimulus.generators.missing

import be.cetic.tsimulus.config.{GeneratorFormat, Model}
import be.cetic.tsimulus.generators.{Generator, TimeToJson}
import be.cetic.tsimulus.timeseries.missing.LimitedTimeSeries
import org.joda.time.LocalDateTime
import spray.json.{JsObject, JsString, JsValue, _}

/**
  * A generator for [[be.cetic.tsimulus.timeseries.missing.LimitedTimeSeries]].
  */
class LimitedGenerator(name: Option[String],
                       val generator: Either[String, Generator[Any]],
                       val from: Option[LocalDateTime],
                       val to: Option[LocalDateTime]) extends Generator[Any](name, "limited")
{
   override def timeseries(generators: (String) => Generator[Any]) =
      LimitedTimeSeries(Model.generator(generators)(generator).timeseries(generators), from, to)

   override def toString = "Limited(" + name + ", " + generator + ", " + from + ", " + to + ")"

   override def equals(o: Any) = o match {
      case that: LimitedGenerator => that.name == this.name &&
         that.generator == this.generator &&
         that.from == this.from &&
         that.to == this.to
      case _ => false
   }

   override def toJson: JsValue =
   {
      val t = Map(
         "type" -> `type`.toJson,
         "generator" -> either2json(generator),
         "from" -> from.get.toJson,
         "to" -> to.get.toJson
      )

      new JsObject(
         name.map(n => t + ("name" -> n.toJson)).getOrElse(t)
      )
   }
}

object LimitedGenerator extends DefaultJsonProtocol with TimeToJson
{
   def apply(value: JsValue): LimitedGenerator = {
      val fields = value.asJsObject.fields

      val name = fields.get("name").map
      {
         case JsString(x) => x
      }

      val generator = fields("generator") match {
         case JsString(s) => Left(s)
         case g => Right(GeneratorFormat.read(g))
      }
      val from = fields.get("from").map(_.convertTo[LocalDateTime])
      val to = fields.get("to").map(_.convertTo[LocalDateTime])
      val missingRate = fields.get("missing-rate").map(_.convertTo[Double])

      new LimitedGenerator(name, generator, from, to)
   }
}
