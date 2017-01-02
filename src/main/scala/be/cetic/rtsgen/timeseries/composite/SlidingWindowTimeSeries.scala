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

package be.cetic.rtsgen.timeseries.composite

import be.cetic.rtsgen.Utils
import be.cetic.rtsgen.timeseries.{IndependantTimeSeries, TimeSeries}
import org.joda.time.{Duration, LocalDateTime}
import com.github.nscala_time.time.Imports._

/**
  * A time series that aggregates recent values of an other time series.
  * A typical use case is a mobile average time series.
  *
  * If no values are available for a given time window, a None value is retrieved.
  *
  * The number of periods in the sliding windows will only rely on the call frequency of this time series, for
  * the underlying time series will only provide values for the times specified by this time series.
  *
  * @param base the underlying time series on which this time series is based.
  * @param duration the time in the past this time series must consider for computing the sliding window.
  *                 Values that relate to times before this will be ignored.
  * @param n    the number of points to consider in the time period.
  * @param aggregator the function used to aggregate values.
  */
case class SlidingWindowTimeSeries[T](base: TimeSeries[T], duration: Duration, n: Int, aggregator: Seq[(Duration, T)] => Option[T]) extends IndependantTimeSeries[T]
{
   override def compute(time: LocalDateTime): Option[T] =
   {
      val start = time - duration
      val dates = Utils.sampling(start, time, n)
      val values = base.compute(dates).map(v => v match {
         case (l: LocalDateTime, Some(x)) => Some((new Duration(l.toDateTime(DateTimeZone.UTC), time.toDateTime(DateTimeZone.UTC)), x))
         case (l: LocalDateTime, None) => None
      } ).flatten
         .toSeq

      aggregator(values)
   }
}