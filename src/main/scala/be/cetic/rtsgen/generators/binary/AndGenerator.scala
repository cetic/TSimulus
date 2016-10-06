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

import be.cetic.rtsgen.config.Model
import be.cetic.rtsgen.generators.Generator
import be.cetic.rtsgen.timeseries.TimeSeries
import be.cetic.rtsgen.timeseries.binary.AndTimeSeries

/**
  * A generator for [[be.cetic.rtsgen.timeseries.binary.AndTimeSeries]].
  */
class AndGenerator(name: Option[String],
                   val a: Either[String, Generator[Any]],
                   val b: Either[String, Generator[Any]]) extends Generator[Any](name, "and")
{
   override def timeseries(generators: (String) => Generator[Any]) =
   {
      val first = Model.generator(generators)(a).timeseries(generators) match {
         case t: TimeSeries[Boolean] => t
      }

      val second = Model.generator(generators)(b).timeseries(generators) match {
         case t: TimeSeries[Boolean] => t
      }

      AndTimeSeries(first, second)
   }

   override def toString = "AndGenerator(" + name + "," + a + "," + b + ")"

   override def equals(o: Any) = o match {
      case that: AndGenerator => that.name == this.name &&
         that.a == this.a &&
         that.b == this.b
      case _ => false
   }
}
