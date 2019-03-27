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

package be.cetic.tsimulus.generators.dt

import be.cetic.tsimulus.config.{GeneratorFormat, Model}
import be.cetic.tsimulus.generators.{Generator, TimeToJson}
import be.cetic.tsimulus.timeseries.TimeSeries
import be.cetic.tsimulus.timeseries.dt.MillisecondTimeSeries
import org.joda.time.LocalDateTime
import spray.json.{DefaultJsonProtocol, JsObject, JsString, JsValue, _}

/**
  * A generator for [[be.cetic.tsimulus.timeseries.dt.MillisecondTimeSeries]].
  *
  * @param name The generator name.
  */
class MillisecondTimeGenerator(name: Option[String], val base: Either[String, Generator[LocalDateTime]]) extends Generator[Int](name, "ms")
{
   override def timeseries(generators: String => Generator[Any]) = {
      val ts = Model.generator(generators)(base).timeseries(generators).asInstanceOf[TimeSeries[LocalDateTime]]
      new MillisecondTimeSeries(ts)
   }

   override def toString = "MillisecondTimeGenerator()"

   override def equals(o: Any) = o match {
      case that: MillisecondTimeGenerator => (that.name == this.name) && (that.base == this.base)
      case _ => false
   }

   override def toJson: JsValue = {

      val t = Map(
         "type" -> `type`.toJson,
         "base" -> either2json(base)
      )

      new JsObject(
         name.map(n => t + ("name" -> n.toJson)).getOrElse(t)
      )
   }
}

object MillisecondTimeGenerator extends DefaultJsonProtocol with TimeToJson
{
   def apply(name: Option[String], base: String) = new MillisecondTimeGenerator(name, Left(base))
   def apply(name: Option[String], base: Generator[LocalDateTime]) = new MillisecondTimeGenerator(name, Right(base))

   def apply(json: JsValue): MillisecondTimeGenerator = {

      val fields = json.asJsObject.fields
      val name = fields.get("name") .map(f => f match {
         case JsString(x) => x
      })

      val base = fields("base") match {
         case JsString(s) => Left(s)
         case g => Right(GeneratorFormat.read(g).asInstanceOf[Generator[LocalDateTime]])
      }

      new MillisecondTimeGenerator(name, base)
   }
}

