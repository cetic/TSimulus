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

import be.cetic.rtsgen.config.Model
import be.cetic.rtsgen.generators.Generator
import be.cetic.rtsgen.timeseries.TimeSeries
import be.cetic.rtsgen.timeseries.composite.DivideTimeSeries

/**
  * A generator for [[be.cetic.rtsgen.timeseries.composite.DivideTimeSeries]].
  */
class DivideGenerator(name: Option[String],
                      val numerator: Either[String, Generator[Any]],
                      val denominator: Either[String, Generator[Any]]) extends Generator[Double](name, "divide")
{
   override def timeseries(gen: String => Generator[Any]) =
   {
      val num = Model.generator(gen)(numerator).timeseries(gen) match {
         case t: TimeSeries[Double] => t
      }

      val den = Model.generator(gen)(denominator).timeseries(gen) match {
         case t: TimeSeries[Double] => t
      }


      new DivideTimeSeries(num, den)
   }

   override def toString = "DivideGenerator(" + name + "," + numerator + "," + denominator + ")"

   override def equals(o: Any) = o match {
      case that: DivideGenerator => that.name == this.name && that.numerator == this.numerator && that.denominator == this.denominator
      case _ => false
   }
}
