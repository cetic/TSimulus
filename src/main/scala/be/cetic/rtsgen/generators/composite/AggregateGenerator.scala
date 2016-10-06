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

package be.cetic.rtsgen.generators

import be.cetic.rtsgen.config._
import be.cetic.rtsgen.timeseries.TimeSeries
import be.cetic.rtsgen.timeseries.composite.AggregationTimeSeries

/**
  * A generator for [[be.cetic.rtsgen.timeseries.composite.AggregationTimeSeries]].
  */
class AggregateGenerator(name: Option[String],
                         val aggregator: String,
                         val generators: Seq[Either[String, Generator[Any]]]) extends Generator[Double](name, "aggregate")
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

      new AggregationTimeSeries[Double](agg, series)
   }

   override def toString = "AggregateGenerator(" + name + "," + aggregator + "," + generators + ")"

   override def equals(o: Any) = o match {
      case that: AggregateGenerator => that.name == this.name && that.aggregator == this.aggregator && that.generators == this.generators
      case _ => false
   }
}
