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

import be.cetic.tsimulus.config._
import be.cetic.tsimulus.generators.{Generator, TimeToJson}
import be.cetic.tsimulus.timeseries.TimeSeries
import be.cetic.tsimulus.timeseries.composite.AggregationTimeSeries
import spray.json.{JsArray, JsObject, JsString, JsValue, _}

/**
  * A generator for [[be.cetic.tsimulus.timeseries.composite.AggregationTimeSeries]].
  */
class AggregateGenerator[U](name: Option[String],
                         val aggregator: String,
                         val generators: Seq[Either[String, Generator[Any]]]) extends Generator[U](name, "aggregate")
{
   override def timeseries(gen: String => Generator[Any]) =
   {
      val agg = aggregationFunction(aggregator)


      val ts = generators.map
      {
         case Left(s) => gen(s).timeseries(gen)
         case Right(g) => g.timeseries(gen)
      }

      val series = ts flatMap {
         case d : TimeSeries[Double] => Some(d)
         case _ => None
      }

      new AggregationTimeSeries[Double, U](agg, series)
   }

   override def toString = "Aggregate(" + name + ", " + aggregator + ", " + generators.mkString("[", ", ", "]") + ")"

   override def equals(o: Any) = o match {
      case that: AggregateGenerator[U] => that.name == this.name && that.aggregator == this.aggregator && that.generators == this.generators
      case _ => false
   }

   override def toJson: JsValue =
   {
      val t = Map(
         "type" -> `type`.toJson,
         "aggregator" -> aggregator.toJson,
         "generators" -> generators.map(either2json).toJson
      )

      new JsObject(
         name.map(n => t + ("name" -> n.toJson)).getOrElse(t)
      )
   }
}

object AggregateGenerator extends DefaultJsonProtocol
{
   def apply[U](value: JsValue): AggregateGenerator[U] = {
      val fields = value.asJsObject.fields

      val name = fields.get("name").map
      {
         case JsString(x) => x
      }

      val aggregator = fields("aggregator").convertTo[String]
      val generators = fields("generators") match {
         case JsArray(x) => x.map
         {
            case JsString(s) => Left(s)
            case g => Right(GeneratorFormat.read(g))
         }.toList
      }

      new AggregateGenerator(name, aggregator, generators)
   }
}
