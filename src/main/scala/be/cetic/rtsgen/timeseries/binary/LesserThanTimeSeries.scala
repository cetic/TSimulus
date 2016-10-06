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

package be.cetic.rtsgen.timeseries.binary

import be.cetic.rtsgen.timeseries.TimeSeries
import org.joda.time.LocalDateTime

/**
  * A time series comparing the values of two time series A and B, and generating
  * true values if the values of A is lesser than the values of B, and
  * false otherwise.
  *
  * If one of the the considered values is undefined, then the generated value is undefined.
  *
  * @param a The first time series.
  * @param b The second time series.
  * @param strict a parameter specifying if the inequality is strict (true) or not (false).
  */
class LesserThanTimeSeries(a: TimeSeries[Double], b: TimeSeries[Double], strict: Boolean = false)
   extends InequalityTimeSeries(
      a,
      b,
      strict match {
         case true  => {(x: Double, y: Double) => x < y}
         case false => {(x: Double, y: Double) => x <= y}
      }
   )