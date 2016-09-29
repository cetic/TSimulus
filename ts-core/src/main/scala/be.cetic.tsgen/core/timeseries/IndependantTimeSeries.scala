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

package be.cetic.tsgen.core.timeseries

import org.joda.time.LocalDateTime

/**
  * A time series generator able to provide each value of the time series independently.
  */
trait IndependantTimeSeries[T] extends TimeSeries[T]
{
   /**
     * @param time A point in the time series
     * @return the value associated to the given time in the time series.
     */
   def compute(time: LocalDateTime): Option[T]

   def compute(times: Stream[LocalDateTime]) = times.map(t => (t, compute(t)))
}
