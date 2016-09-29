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

package be.cetic.tsgen.core.timeseries.composite

import be.cetic.tsgen.core.timeseries.TimeSeries
import org.joda.time.LocalDateTime

import scala.util.Random

/**
  * This time series is built as a correlation of an other time series.
  *
  * See http://www.sitmo.com/article/generating-correlated-random-numbers/ for explainations.
  *
  * @param base the time series generator on which this generator is based.
  * @param seed the value used as seed for the random number generator. For a fixed seed and a fixed time series,
  *             the correlated values are deterministically generated.
  * @param rho  the correlation coefficient determining the *strongness* of the correlation. Must be in [0, 1]
  */
case class CorrelatedTimeSeries(base: TimeSeries[Double],
                                seed: Int,
                                rho: Double) extends TimeSeries[Double]
{
   val rho_square = rho*rho

   override def compute(times: Stream[LocalDateTime]) =
   {
      val r = new Random(seed)
      base.compute(times)
          .map {case(t,v) => (t, v.map(a => (rho * a) + (math.sqrt(1 - rho_square) * r.nextGaussian)))}
   }
}
