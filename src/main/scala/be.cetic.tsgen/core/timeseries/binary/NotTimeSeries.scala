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

package be.cetic.tsgen.core.timeseries.binary

import be.cetic.tsgen.core.timeseries.TimeSeries
import org.joda.time.LocalDateTime

/**
  * A binary time series which is the negation of an other time series.
  * This time series is true iff the base time series is false.
  *
  * If the value is not defined for the base time series, then the NOT value is not defined.
  */
case class NotTimeSeries(base: TimeSeries[Boolean]) extends TimeSeries[Boolean]
{
   override def compute(times: Stream[LocalDateTime]) = base.compute(times).map { case (t,v) => (t, v.map(!_)) }
}
