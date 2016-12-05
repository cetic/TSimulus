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

package be.cetic.rtsgen.test.timeseries.missing

import be.cetic.rtsgen.test.RTSTest
import be.cetic.rtsgen.timeseries.composite.ConditionalTimeSeries
import be.cetic.rtsgen.timeseries.primary.ConstantTimeSeries
import org.scalatest.{FlatSpec, Inspectors, Matchers}

class ConditionalTimeSeriesTest extends FlatSpec with Matchers with Inspectors with RTSTest
{
   "A successful conditional time series" should "use the success time series" in {
      forAll(ConditionalTimeSeries(t, t, f).compute(dates)) {
         result => result._2 shouldBe Some(true)
      }
   }

   "An unsuccessful conditional time series" should "use the failure time series" in {
      forAll(ConditionalTimeSeries(f, t, f).compute(dates)) {
         result => result._2 shouldBe Some(false)
      }
   }

   "A undetermined conditional time series" should "use the success time series" in {
      forAll(ConditionalTimeSeries(u, t, f).compute(dates)) {
         result => result._2 shouldBe None
      }
   }
}
