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

/**
  * A binary time series based on two time series. This time series is true iff both
  * base time series are true.
  *
  * If the value is not defined for at least one of the base time series, then the AND value is also undefined.
  */
class AndTimeSeries(a: TimeSeries[Boolean],
                    b: TimeSeries[Boolean]) extends BinaryTimeSeries[Boolean, Boolean](a, b, (x,y) => if(x.isEmpty || y.isEmpty) None
                                                                                                      else Some(x.get && y.get))