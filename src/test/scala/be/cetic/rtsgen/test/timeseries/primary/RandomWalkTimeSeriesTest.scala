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

package be.cetic.rtsgen.test.timeseries.primary

import be.cetic.rtsgen.Utils
import be.cetic.rtsgen.test.RTSTest
import be.cetic.rtsgen.timeseries.primary.{ARMA, RandomWalkTimeSeries}
import org.joda.time.LocalDateTime
import org.scalatest.{FlatSpec, Inspectors, Matchers}
import com.github.nscala_time.time.Imports._


class RandomWalkTimeSeriesTest extends FlatSpec with Matchers with Inspectors with RTSTest
{
   "A random walk time series" should "provide identical results, by each moment at a time, or on a batch, when the origin is before the dates" in {
      val dates = Utils.sampling(new LocalDateTime(2016, 1, 1, 0, 0), new LocalDateTime(2016, 1, 2, 0, 0), 100)
      val ts = new RandomWalkTimeSeries(ARMA(Array(), Array(), 0.01, 0, 42), new LocalDateTime(2015, 12, 30, 0, 0), 1 minute)

      val individuals = dates.map(d => ts.compute(d).get)
      val batched = ts.compute(dates).map(_._2.get)

      individuals shouldBe batched
   }

   "A random walk time series" should "provide identical results, by each moment at a time, or on a batch, when the origin is among the dates" in {
      val dates = Utils.sampling(new LocalDateTime(2016, 1, 1, 0, 0), new LocalDateTime(2016, 1, 2, 0, 0), 100)
      val ts = new RandomWalkTimeSeries(ARMA(Array(), Array(), 0.01, 0, 42), new LocalDateTime(2016, 1, 1, 2, 0), 1 minute)

      val individuals = dates.map(d => ts.compute(d).get)
      val batched = ts.compute(dates).map(_._2.get)

      individuals shouldBe batched
   }

   "A random walk time series" should "provide identical results, by each moment at a time, or on a batch, when the origin is after the dates" in {
      val dates = Utils.sampling(new LocalDateTime(2016, 1, 1, 0, 0), new LocalDateTime(2016, 1, 2, 0, 0), 100)
      val ts = new RandomWalkTimeSeries(ARMA(Array(), Array(), 0.01, 0, 42), new LocalDateTime(2016, 2, 1, 0, 0), 1 minute)

      val individuals = dates.map(d => ts.compute(d).get)
      val batched = ts.compute(dates).map(_._2.get)

      individuals shouldBe batched
   }
}
