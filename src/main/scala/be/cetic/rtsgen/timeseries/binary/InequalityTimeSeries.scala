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
  * A time series that compares the values of two time series using an arbitrary comparator.
  * @param a   A time series.
  * @param b   An other time series.
  * @param comparator The comparator used to determine which value must be generated.
  */
case class InequalityTimeSeries( val a: TimeSeries[Double],
                                 val b: TimeSeries[Double],
                                 val comparator: (Double, Double) => Boolean
                               ) extends TimeSeries[Boolean]
{
   override def compute(times: Stream[LocalDateTime]): Stream[(LocalDateTime, Option[Boolean])] =
   {
      val aSeries = a.compute(times)
      val bSeries = b.compute(times)

      (aSeries zip bSeries).map { case (x,y) => {
         val time = x._1
         assert(time equals y._1)

         val value = if(x._2.isEmpty || y._2.isEmpty) None
                     else Some(comparator(x._2.get, y._2.get))

         (time, value)
      }}
   }

   override def compute(time: LocalDateTime): Option[Boolean] =
   {
      val x = a.compute(time)
      val y = b.compute(time)

      if(x.isEmpty || y.isEmpty) None
      else Some(comparator(x.get, y.get))
   }
}
