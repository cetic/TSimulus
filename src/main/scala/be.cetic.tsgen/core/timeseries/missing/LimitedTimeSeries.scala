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

package be.cetic.tsgen.core.timeseries.missing

import be.cetic.tsgen.core.timeseries.TimeSeries
import com.github.nscala_time.time.Imports._
import org.joda.time.LocalDateTime


/**
  * A time series in which there is a "hole": no value are generated while in the hole.
  * When not in the hole, the generated values are provided by a base time series.
  */
case class LimitedTimeSeries[T] (base: TimeSeries[T], from: Option[LocalDateTime], to: Option[LocalDateTime]) extends TimeSeries[T]
{
   override def compute(times: Stream[LocalDateTime]): Stream[(LocalDateTime, Option[T])] =
   {
      base.compute(times).map {case (t,v) => {

         val fromCondition = from match {
            case None => true
            case Some(x) => t >= x
         }

         val toCondition = to match {
            case None => true
            case Some(x) => t <= x
         }

         val modified = if(fromCondition && toCondition) None
                        else v

         (t, modified)
      }}
   }
}
