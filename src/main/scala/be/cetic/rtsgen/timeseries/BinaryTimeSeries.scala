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

package be.cetic.rtsgen.timeseries

import org.joda.time.LocalDateTime

/**
  * This trait is a helper for time series based on two different underlying time series.
  */
case class BinaryTimeSeries[T,R](a: TimeSeries[T], b: TimeSeries[T], operator: (Option[T],Option[T]) => Option[R]) extends TimeSeries[R]
{
   override def compute(time: LocalDateTime) = operator(a.compute(time), b.compute(time))

   override def compute(times: Stream[LocalDateTime]) =
   {
      val xs = a.compute(times)
      val ys = b.compute(times)

      (xs zip ys) map {
         case (x,y) =>
         {
            assert(x._1 == y._1)

            (x._1, operator(x._2, y._2))
         }
      }
   }
}
