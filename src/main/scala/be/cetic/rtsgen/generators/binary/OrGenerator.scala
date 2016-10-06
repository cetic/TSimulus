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

import be.cetic.rtsgen.timeseries.TimeSeries
import be.cetic.rtsgen.timeseries.binary.OrTimeSeries

/**
  * Created by Mathieu Goeminne.
  */
class OrGenerator(name: Option[String],
                  val a: Either[String, Generator[Any]],
                  val b: Either[String, Generator[Any]]) extends Generator[Any](name, "or")
{
   override def timeseries(generators: (String) => Generator[Any]) =
   {
      val first = Model.generator(generators)(a).timeseries(generators) match {
         case t: TimeSeries[Boolean] => t
      }

      val second = Model.generator(generators)(b).timeseries(generators) match {
         case t: TimeSeries[Boolean] => t
      }

      OrTimeSeries(first, second)
   }

   override def toString = "OrGenerator(" + name + "," + a + "," + b + ")"

   override def equals(o: Any) = o match {
      case that: OrGenerator => that.name == this.name &&
         that.a == this.a &&
         that.b == this.b
      case _ => false
   }
}
