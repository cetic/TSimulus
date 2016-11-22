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
import be.cetic.rtsgen.generators.{Generator, TimeToJson}
import be.cetic.rtsgen.timeseries.TimeSeries
import be.cetic.rtsgen.timeseries.binary.LogisticTimeSeries
import spray.json.{JsObject, JsString, JsValue, _}

import scala.util.Random

/**
  * A generator for [[be.cetic.rtsgen.timeseries.binary.LogisticTimeSeries]].
  */
class LogisticGenerator(name: Option[String],
                        val generator: Either[String, Generator[Any]],
                        val location: Double,
                        val scale: Double,
                        val seed: Option[Int]) extends Generator[Boolean](name, "logistic")
{

   override def timeseries(generators: (String) => Generator[Any]) =
   {
      Model.generator(generators)(generator).timeseries(generators) match {
         case dTS: TimeSeries[Double] => LogisticTimeSeries(dTS, location, scale, seed.getOrElse(Random.nextInt()))
         case other => throw new ClassCastException(other.toString)
      }
   }

   override def toString = "Logistic(" + name + ", " + generator + ", " + location + ", " + scale + ", " + seed + ")"

   override def equals(o: Any) = o match {
      case that: LogisticGenerator => that.name == this.name &&
         that.generator == this.generator &&
         that.location == this.location &&
         that.scale == this.scale &&
         that.seed == this.seed
      case _ => false
   }

   override def toJson: JsValue = {
      val _generator = (generator match {
         case Left(s) => s.toJson
         case Right(g) => g.toJson
      }).toJson

      val t = Map(
         "type" -> `type`.toJson,
         "generator" -> _generator,
         "location" -> location.toJson,
         "scale" -> scale.toJson,
         "seed" -> seed.toJson
      )

      new JsObject(
         name.map(n => t + ("name" -> n.toJson)).getOrElse(t)
      )
   }
}

object LogisticGenerator extends TimeToJson
{
   def apply(value: JsValue): LogisticGenerator = {
      val fields = value.asJsObject.fields

      val name = fields.get("name").map
      {
         case JsString(x) => x
      }

      val generator = fields("generator") match {
         case JsString(s) => Left(s)
         case g => Right(GeneratorFormat.read(g))
      }
      val location = fields("location").convertTo[Double]
      val scale = fields("scale").convertTo[Double]
      val seed = fields.get("seed").map(_.convertTo[Int])

      new LogisticGenerator(name, generator, location, scale, seed)
   }
}
