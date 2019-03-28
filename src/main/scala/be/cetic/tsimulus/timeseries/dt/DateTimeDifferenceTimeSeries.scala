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

package be.cetic.tsimulus.timeseries.dt

import be.cetic.tsimulus.timeseries.TimeSeries
import org.joda.time.{DateTimeZone, LocalDateTime, Duration}

/**
  * A time series representing the difference between two LocalDateTime time series.
  */
class DateTimeDifferenceTimeSeries(a: TimeSeries[LocalDateTime], b: TimeSeries[LocalDateTime]) extends TimeSeries[Duration]
{
   override def compute(times: Stream[LocalDateTime]): Stream[(LocalDateTime, Option[Duration])] = {
      val z = a.compute(times) zip b.compute(times)

      z.map(s => {
         if(s._1._2.isEmpty || s._2._2.isEmpty) (s._1._1, None)
         else (s._1._1, Some(new Duration(s._1._2.get.toDateTime(DateTimeZone.UTC), s._2._2.get.toDateTime(DateTimeZone.UTC))))
      })
   }

   override def compute(time: LocalDateTime): Option[Duration] = {
      val aTime = a.compute(time)
      val bTime = b.compute(time)

      if (aTime.isEmpty || bTime.isEmpty) None
      else Some(new Duration(aTime.get.toDateTime(DateTimeZone.UTC), bTime.get.toDateTime(DateTimeZone.UTC)))
   }
}
