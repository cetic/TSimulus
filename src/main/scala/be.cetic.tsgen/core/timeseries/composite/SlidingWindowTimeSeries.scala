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
  * @param aggregator the function used to aggregate values.
  */
case class SlidingWindowTimeSeries[T](base: TimeSeries[T], duration: Duration, aggregator: Seq[(Duration, T)] => T) extends TimeSeries[T]
{

   override def compute(times: Stream[LocalDateTime]): Stream[(LocalDateTime, Option[T])] =
   {
      def addElement[T](elem: (LocalDateTime, Option[T]),
                        buffer: List[(LocalDateTime, T)],
                        predicate: ((LocalDateTime, T)) => Boolean): List[(LocalDateTime, T)] =
      {
         val completed = elem match {
            case (t: LocalDateTime, Some(v)) => (t, v) :: buffer
            case _ => buffer
         }

         completed takeWhile (predicate)
      }

      val data = base.compute(times)

      data.scanLeft(new LocalDateTime(), (List[(LocalDateTime, T)]()))((oldBuffer, entry) => {
         val limit = entry._1 - duration
         val predicate = (x: (LocalDateTime, T)) => x._1 >= limit

         (entry._1, addElement(entry, oldBuffer._2, predicate))
      }).map
      {
         case (t: LocalDateTime, List()) => (t, None)
         case (t: LocalDateTime, v: List[(LocalDateTime, T)]) =>
            (
               t,
               Some(
                  aggregator(v.map(element => ((element._1.toDateTime(DateTimeZone.UTC) to t.toDateTime(DateTimeZone.UTC)).toDuration, element._2)))
               )
            )
      }
   }
}