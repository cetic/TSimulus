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

package be.cetic.rtsgen.timeseries.missing

import be.cetic.rtsgen.timeseries.TimeSeries
import com.github.nscala_time.time.Imports._
import org.joda.time.LocalDateTime

import scala.util.Random

/**
  * A time series that only produces values during a given period.
  *
  * @param base The time series on which this time series is based.
  * @param from The date from which values are produced.
  * @param to The date to which which values are produced.
  * @param missingRate The frequency at which a value is not produced, despite the fact that
  *                    it should be produced according to the specified limit dates.
  */
case class PartialTimeSeries[T](base: TimeSeries[T],
                                from: Option[LocalDateTime],
                                to: Option[LocalDateTime],
                                missingRate: Option[Double]) extends TimeSeries[T]
{
   override def compute(times: Stream[LocalDateTime]) =
   {

      base.compute(times).map
      { case (t, v) =>
      {
         (t, v.map(x => {
            if ((from.isDefined && t < from.get) || (to.isDefined && t > to.get)) None
            else
            {
               missingRate match {
                  case None => Some(x)
                  case Some(odds) => if (Random.nextDouble() < odds) None
                                     else Some(x)
               }
            }
         }).flatten)
      }
      }
   }
}
