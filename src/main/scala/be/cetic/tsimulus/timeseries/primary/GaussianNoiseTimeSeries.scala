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

package be.cetic.tsimulus.timeseries.primary

import java.util.Random

import be.cetic.tsimulus.timeseries.IndependantTimeSeries
import org.joda.time.{DateTimeZone, LocalDateTime}

/**
  * A time series based on a gaussian noise.
  *
  * It produces unrelated value for each given time in the time series, and is therefore less realistic
  * than a Random Walk time series. However, contrary to a random walk, each value may be evaluated independently.
  * The gaussian noise is therefore more efficient than a random walk when a value must be repeatedly be evaluated.
  *
  * @param seed The seed used to defined how values must be randomly generated.
  * @param std  The standard deviation of the gaussian distribution values must respect.
  */
case class GaussianNoiseTimeSeries(seed: Int, std: Double) extends IndependantTimeSeries[Double]
{
   override def compute(time: LocalDateTime): Option[Double] =
   {
      val r = new Random(seed + time.toDateTime(DateTimeZone.UTC).getMillis)
      Some(r.nextGaussian() * std)
   }
}
