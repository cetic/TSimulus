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
import be.cetic.tsimulus.timeseries.missing.PartialTimeSeries
import org.joda.time.LocalDateTime
import spray.json.{JsObject, JsString, JsValue, _}

/**
  * A generator for [[be.cetic.tsimulus.timeseries.missing.PartialTimeSeries]].
  */
class PartialGenerator(name: Option[String],
                       val generator: Either[String, Generator[Any]],
                       val from: Option[LocalDateTime],
                       val to: Option[LocalDateTime],
                       val missingRate: Option[Double]) extends Generator[Any](name, "partial")
{
   override def timeseries(generators: (String) => Generator[Any]) =
   {
      val ts = Model.generator(generators)(generator).timeseries(generators)
      PartialTimeSeries(ts, from, to, missingRate)
   }

   override def toString = "Partial(" + name + ", " + generator + ", " + from + ", " + to + ", " + missingRate + ")"

   override def equals(o: Any) = o match {
      case that: PartialGenerator => that.name == this.name &&
         that.generator == this.generator &&
         that.from == this.from &&
         that.to == this.to &&
         that.missingRate == this.missingRate
      case _ => false
   }

   override def toJson: JsValue =
   {
     var t = Map(
         "type" -> `type`.toJson,
         "generator" -> either2json(generator),
         "from" -> from.toJson,
         "to" -> to.toJson
      )

      if(missingRate.isDefined) t = t.updated("missing-rate" , missingRate.toJson)
      if(name.isDefined) t = t.updated("name", name.toJson)

      new JsObject(t)
   }
}

object PartialGenerator extends DefaultJsonProtocol with TimeToJson
{
   def apply(value: JsValue): PartialGenerator = {
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

      new PartialGenerator(name, generator, from, to, missingRate)
   }
}
