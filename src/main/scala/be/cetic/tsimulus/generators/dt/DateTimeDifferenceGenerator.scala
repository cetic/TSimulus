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
import be.cetic.tsimulus.timeseries.dt.DateTimeDifferenceTimeSeries
import org.joda.time.{Duration, LocalDateTime}
import spray.json.{DefaultJsonProtocol, JsObject, JsString, JsValue, _}

/**
  * A generator for [[be.cetic.tsimulus.timeseries.dt.DateTimeDifferenceTimeSeries]].
  *
  * Computes the difference between two datetime time series.
  *
  * @param name The generator name.
  * @param a One of the datetime generator.
  * @param b The other datetime generator.
  */
class DateTimeDifferenceGenerator(name: Option[String], val a: Either[String, Generator[LocalDateTime]], val b: Either[String, Generator[LocalDateTime]]) extends Generator[Duration](name, "dt::diff")
{
   override def timeseries(generators: String => Generator[Any]) = {
      val aTS = Model.generator(generators)(a).timeseries(generators).asInstanceOf[TimeSeries[LocalDateTime]]
      val bTS = Model.generator(generators)(b).timeseries(generators).asInstanceOf[TimeSeries[LocalDateTime]]
      new DateTimeDifferenceTimeSeries(aTS, bTS)
   }

   override def toString = s"DateTimeDifferenceGenerator(${a}, ${b})"

   override def equals(o: Any) = o match {
      case that: DateTimeDifferenceGenerator => that.name == this.name && this.a == that.a && this.b == that.b
      case _ => false
   }

   override def toJson: JsValue = {

      val t = Map(
         "type" -> `type`.toJson,
         "a" -> either2json(a),
         "b" -> either2json(b)
      )

      new JsObject(
         name.map(n => t + ("name" -> n.toJson)).getOrElse(t)
      )
   }
}

object DateTimeDifferenceGenerator extends DefaultJsonProtocol with TimeToJson
{
   def apply(name: Option[String], a: String, b: String) = new DateTimeDifferenceGenerator(name, Left(a), Left(b))
   def apply(name: Option[String], a: String, b: Generator[LocalDateTime]) = new DateTimeDifferenceGenerator(name, Left(a), Right(b))
   def apply(name: Option[String], a: Generator[LocalDateTime], b: String) = new DateTimeDifferenceGenerator(name, Right(a), Left(b))
   def apply(name: Option[String], a: Generator[LocalDateTime], b: Generator[LocalDateTime]) = new DateTimeDifferenceGenerator(name, Right(a), Right(b))

   def apply(json: JsValue): DateTimeDifferenceGenerator = {

      val fields = json.asJsObject.fields
      val name = fields.get("name") .map(f => f match {
         case JsString(x) => x
      })

      val a = fields("a") match {
         case JsString(s) => Left(s)
         case g => Right(GeneratorFormat.read(g).asInstanceOf[Generator[LocalDateTime]])
      }

      val b = fields("b") match {
         case JsString(s) => Left(s)
         case g => Right(GeneratorFormat.read(g).asInstanceOf[Generator[LocalDateTime]])
      }

      new DateTimeDifferenceGenerator(name, a, b)
   }
}


