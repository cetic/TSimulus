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
import be.cetic.rtsgen.timeseries.binary.NotTimeSeries

/**
  * Created by Mathieu Goeminne.
  */
class NotGenerator(name: Option[String],
                   val generator: Either[String, Generator[Any]]) extends Generator[Any](name, "or")
{
   override def timeseries(generators: (String) => Generator[Any]) =
   {
      val base = Model.generator(generators)(generator).timeseries(generators) match {
         case t: TimeSeries[Boolean] => t
      }

      NotTimeSeries(base)
   }

   override def toString = "Notenerator(" + name + "," + generator + ")"

   override def equals(o: Any) = o match {
      case that: NotGenerator => that.name == this.name &&
         that.generator == this.generator
      case _ => false
   }
}
