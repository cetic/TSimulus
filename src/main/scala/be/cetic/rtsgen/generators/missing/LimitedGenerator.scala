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

package be.cetic.rtsgen.generators.missing

import be.cetic.rtsgen.config.Model
import be.cetic.rtsgen.generators.Generator
import be.cetic.rtsgen.timeseries.missing.LimitedTimeSeries
import org.joda.time.LocalDateTime

/**
  * A generator for [[be.cetic.rtsgen.timeseries.missing.LimitedTimeSeries]].
  */
class LimitedGenerator(name: Option[String],
                       val generator: Either[String, Generator[Any]],
                       val from: Option[LocalDateTime],
                       val to: Option[LocalDateTime]) extends Generator[Any](name, "limited")
{
   override def timeseries(generators: (String) => Generator[Any]) =
      LimitedTimeSeries(Model.generator(generators)(generator).timeseries(generators), from, to)

   override def toString = "LimitedGenerator(" + name + "," + generator + "," + from + "," + to + ")"

   override def equals(o: Any) = o match {
      case that: LimitedGenerator => that.name == this.name &&
         that.generator == this.generator &&
         that.from == this.from &&
         that.to == this.to
      case _ => false
   }
}
