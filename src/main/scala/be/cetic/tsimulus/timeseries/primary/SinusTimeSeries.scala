/*
 * Copyright Cetic ASBL
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

package be.cetic.tsimulus.timeseries.primary

import java.time.ZoneId

import be.cetic.tsimulus.timeseries.IndependantTimeSeries
import org.joda.time.{DateTimeZone, LocalDateTime}

/**
  * A time series that produces a sinusoidal signal.
  *
  * @param origin The moment that corresponds to the origin of the signal frame.
  *               By definition, the value associated with the time series at the origin is zero.
  *
  * @param period The duration, in milliseconds, correspond to a signal period.
  *               By definition, the value associated with the time series will by zero at origin + k*period,
  *               for any integer k.
  */
case class SinusTimeSeries(origin: LocalDateTime, period: Long) extends IndependantTimeSeries[Double]
{
   val origin_ms = origin.toDateTime(DateTimeZone.UTC).getMillis

   /**
     * Calculates the value of the time series for a single date.
     *
     * @param time the moment for which the time series value must be calculated.
     * @return the value associated to the specified moment, if defined.
     */
   override def compute(time: LocalDateTime): Option[Double] =
   {
      val target_ms = time.toDateTime(DateTimeZone.UTC).getMillis
      val k = Math.floor((target_ms - origin_ms).toDouble / period).toLong

      val frame = origin_ms + k * period
      val delta = target_ms - frame

      assert(delta >= 0)
      assert(delta < period)

      val ratio = 2.0 * Math.PI * (delta.toDouble / period)
      Some(Math.sin(ratio))
   }
}
