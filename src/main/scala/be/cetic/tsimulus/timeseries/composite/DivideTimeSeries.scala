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

package be.cetic.tsimulus.timeseries.composite

import be.cetic.tsimulus.timeseries.{BinaryTimeSeries, TimeSeries}
import org.joda.time.LocalDateTime

/**
  * This time series generator divides a time series by an other one.
  *
  * @param numerator the generator that represents the numerator.
  * @param denominator the generator that represents the denominator.
  */
class DivideTimeSeries(numerator: TimeSeries[Double],
                       denominator: TimeSeries[Double]) extends BinaryTimeSeries[Double, Double](numerator, denominator, (x,y) => if(x.isEmpty || y.isEmpty) None
                                                                                                                                  else Some(x.get / y.get))
