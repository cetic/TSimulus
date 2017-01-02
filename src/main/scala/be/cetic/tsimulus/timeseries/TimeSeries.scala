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

package be.cetic.tsimulus.timeseries

import org.joda.time.LocalDateTime

/**
  * A cyclic time series
  */
trait TimeSeries[+T]
{
   /**
     * Calculates the values of the time series of a series of moments.
     *
     * @param times a series of moments for which values must be calculated. Each time must be greater than or equal to
     *              the previous one.
     * @return the values associated to the specified moments, if defined.
     */
   def compute(times: Stream[LocalDateTime]): Stream[(LocalDateTime, Option[T])]

   /**
     * Calculates the value of the time series for a single date.
     *
     * @param time the moment for which the time series value must be calculated.
     * @return the value associated to the specified moment, if defined.
     */
   def compute(time: LocalDateTime): Option[T]
}
