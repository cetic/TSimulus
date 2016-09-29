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

import scala.util.Random

/**
  * A time series of a binary variable, based on the correlation between this variable and a continuous one.
  *
  * This time series is based on a LOGIT model predicting the value of the binary variable based on a linear regression
  * of the continuous variable.
  *
  * @param base: the time series on which this one is based.
  * @param location: a parameter affecting the location of the underlying LOGIT model. It corresponds to the mean (and the median)
  *                  of this model.
  * @param scale: a parameter affecting the scale of the underlying LOGIT model.
  *               Must be not null. Must be strictly greater than 0 to get a positive correlation between
  *               the binary variable and the continuous one.
  * @param seed the seed used to produce random values deterministically.
  */
case class LogisticTimeSeries(base: TimeSeries[Double],
                              location: Double,
                              scale: Double,
                              seed: Int) extends TimeSeries[Boolean]
{


   /**
     * @param times a series of time for which values must be computed. Each time must be greater than or equal to
     *              the previous one.
     * @return the values associated to the specified times.
     */
   override def compute(times: Stream[LocalDateTime]) =
   {
      val r = new Random(seed)
      def logit(x: Double) = 1 / (1 + Math.exp(- ((x - location) / scale)))

      base.compute(times).map { case (t,v) => (t, v.map(x => r.nextDouble() < logit(x) ))}
   }
}
