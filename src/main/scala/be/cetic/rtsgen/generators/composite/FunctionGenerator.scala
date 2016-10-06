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
import be.cetic.rtsgen.timeseries.composite.FunctionTimeSeries

class FunctionGenerator(name: Option[String],
                        val generator: Either[String, Generator[Any]],
                        val slope: Double,
                        val intercept: Double) extends Generator[Double](name, "function")
{
   override def timeseries(generators: String => Generator[Any]) =
   {
      Model.generator(generators)(generator) match {
         // Could also be expressed as a Sum(Times(generator, Constant(slope), intercept)
         case g: Generator[Double] => FunctionTimeSeries[Double](g.timeseries(generators), (t,v) => Some(slope * v + intercept))
         case _ => throw new ClassCastException
      }
   }

   override def toString = "FunctionGenerator(" + name + "," + generator + "," + slope + "," + intercept + ")"

   override def equals(o: Any) = o match {
      case that: FunctionGenerator => (that.name == this.name &&
         that.generator == this.generator &&
         that.slope == this.slope &&
         that.intercept == this.intercept)
      case _ => false
   }
}
