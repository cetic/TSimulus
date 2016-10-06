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

package be.cetic.rtsgen.test.timeseries.binary

import be.cetic.rtsgen.test.RTSTest
import be.cetic.rtsgen.timeseries.binary.ArbitraryBinaryTimeSeries
import org.scalatest.{FlatSpec, Inspectors, Matchers}

class ArbitraryBinaryTimeSeriesTest extends FlatSpec with Matchers with Inspectors with RTSTest
{
   "An arbitrary binary time series with 'always true' predicate" should "generate true values" in {
      forAll (ArbitraryBinaryTimeSeries(t, {x: Boolean => true}).compute(dates)) { result => result._2 shouldBe Some(true)}
   }

   "An arbitrary binary time series with 'always false' predicate" should "generate false values" in {
      forAll (ArbitraryBinaryTimeSeries(t, {x: Boolean => false}).compute(dates)) { result => result._2 shouldBe Some(false)}
   }
}
