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
  * A time series based on two binary time series and applying the "equivalent" (===) operator.
  *
  * If at least one of the considered time series has unspecified value, the resulting value is unspecified, too.
  *
  * @param a An other time series.
  * @param b An other binary time series
  */
case class EquivTimeSeries(a: TimeSeries[Boolean], b: TimeSeries[Boolean]) extends TimeSeries[Boolean]
{
   override def compute(times: Stream[LocalDateTime]): Stream[(LocalDateTime, Option[Boolean])] = {
      val vA = a.compute(times)
      val vB = b.compute(times)

      (vA zip vB).map(element => {
         val timestamp = element._1._1

         val valueA = element._1._2
         val valueB = element._2._2

         val value = if(valueA.isEmpty || valueB.isEmpty) None
                     else Some(valueA.get == valueB.get)

         (timestamp, value)
      })
   }

   override def compute(time: LocalDateTime): Option[Boolean] =
   {
      val x = a.compute(time)
      val y = b.compute(time)

      if(x.isEmpty || y.isEmpty) None
      else Some(x.get == y.get)

   }
}
