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

package be.cetic.tsgen.core.config.core.timeseries.composite

import be.cetic.tsgen.core.config.core.timeseries.TimeSeries
import com.github.nscala_time.time.Imports._
import org.joda.time.{Duration, LocalDateTime}

/**
  * A time series based on an other time series, and for which time is shifted.
 *
  * @param generator The base time series.
  * @param shift The time shift to apply, so that this.compute(t) == generator.compute(t+shift);
  * @tparam T
  */
case class TimeShiftTimeSeries[T](generator: TimeSeries[T], shift: Duration) extends TimeSeries[T]
{
   override def compute(times: Stream[LocalDateTime]): Stream[(LocalDateTime, Option[T])] =
      generator.compute(times.map(t => t + shift))
}