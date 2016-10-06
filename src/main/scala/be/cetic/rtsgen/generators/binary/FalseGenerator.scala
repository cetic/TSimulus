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

import be.cetic.rtsgen.generators.Generator
import be.cetic.rtsgen.timeseries.binary.FalseTimeSeries

/**
  * A generator for [[be.cetic.rtsgen.timeseries.binary.FalseTimeSeries]].
  */
class FalseGenerator(name: Option[String]) extends Generator[Boolean](name, "false")
{

   override def timeseries(generators: (String) => Generator[Any]) = new FalseTimeSeries()

   override def toString = "FalseGenerator(" + name + ")"

   override def equals(o: Any) = o match {
      case that: FalseGenerator => that.name == this.name
      case _ => false
   }
}
