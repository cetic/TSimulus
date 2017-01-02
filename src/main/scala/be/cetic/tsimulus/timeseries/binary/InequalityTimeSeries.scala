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

import be.cetic.tsimulus.timeseries.{BinaryTimeSeries, TimeSeries}
import org.joda.time.LocalDateTime

/**
  * A time series that compares the values of two time series using an arbitrary comparator.
  * @param a   A time series.
  * @param b   An other time series.
  * @param comparator The comparator used to determine which value must be generated.
  */
class InequalityTimeSeries( a: TimeSeries[Double],
                            b: TimeSeries[Double],
                            val comparator: (Double, Double) => Boolean
) extends BinaryTimeSeries[Double, Boolean](a, b, (x,y) => if(x.isEmpty || y.isEmpty) None
                                                           else Some(comparator(x.get, y.get)))