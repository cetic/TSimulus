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

package be.cetic.tsgen.core.timeseries.composite

import be.cetic.tsgen.core.timeseries.TimeSeries
import org.joda.time.LocalDateTime

/**
  * This time series generator divides a time series by an other one.
  *
  * @param numerator the generator that represents the numerator.
  * @param denominator the generator that represents the denominator.
  */
class DivideTimeSeries(val numerator: TimeSeries[Double], val denominator: TimeSeries[Double]) extends TimeSeries[Double]
{
   override def compute(times: Stream[LocalDateTime]) =
   {
      val fractions = (numerator.compute(times) zip denominator.compute(times)).map( entry => entry._1._1 -> (entry._1._2, entry._2._2))

      fractions.map {
         case (time, (None, _)) => (time, None)
         case (time, (_, None)) => (time, None)
         case (time, (_, Some(0))) => (time, None)
         case (time, (Some(num), Some(den))) => (time, Some(num / den))
      }
   }

   override def toString = "DivideTimeSeries(" + numerator + "," + denominator + ")"
}