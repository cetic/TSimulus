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

import be.cetic.rtsgen.config.Model
import be.cetic.rtsgen.timeseries.TimeSeries
import be.cetic.rtsgen.timeseries.missing.DefaultTimeSeries

/**
  * A generator for the Default time series.
  */
class DefaultGenerator(name: Option[String], val gens: Seq[Either[String, Generator[Any]]]) extends Generator[Any](name, "first-of")
{
   override def timeseries(generators: (String) => Generator[Any]) =
   {
      val underlyings = gens.map(g => Model.generator(generators)(g).timeseries(generators) match {
         case t: TimeSeries[Any] => t
      })

      DefaultTimeSeries(underlyings)
   }

   override def toString = "UndefinedGenerator(" + name + "," + gens + ")"

   override def equals(o: Any) = o match {
      case that: DefaultGenerator => that.gens == this.gens
      case _ => false
   }
}
