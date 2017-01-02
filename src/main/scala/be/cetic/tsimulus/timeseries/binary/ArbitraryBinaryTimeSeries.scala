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

package be.cetic.tsimulus.timeseries.binary

import be.cetic.tsimulus.timeseries.TimeSeries
import org.joda.time.LocalDateTime

/**
  * A binary time series based on an arbitrary predicate. The returned values will be false if the
  * predicate is respected for the underlying values.
  *
  * @param base the original time series
  * @param predicate the predicate to use for determining if the generated binary value must be true or false.
  */
case class ArbitraryBinaryTimeSeries[T](base: TimeSeries[T], predicate: T => Boolean) extends TimeSeries[Boolean]
{
   override def compute(times: Stream[LocalDateTime]) = base.compute(times).map { case (t,v) => (t,v.map(predicate))}

   override def compute(time: LocalDateTime): Option[Boolean] = base.compute(time).map(predicate)
}
