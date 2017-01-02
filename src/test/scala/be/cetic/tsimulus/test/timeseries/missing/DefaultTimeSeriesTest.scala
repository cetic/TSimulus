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

package be.cetic.tsimulus.test.timeseries.missing

import be.cetic.tsimulus.test.RTSTest
import be.cetic.tsimulus.timeseries.missing.DefaultTimeSeries
import org.scalatest.{FlatSpec, Inspectors, Matchers}

class DefaultTimeSeriesTest extends FlatSpec with Matchers with Inspectors with RTSTest
{
   "A default time series with undefined values first" should "skip to the first defined values" in {
      forAll (DefaultTimeSeries(Seq(u, t, f)).compute(dates)) { result => result._2 shouldBe Some(true)}
   }

   "An empty default time series" should "generate undefined values" in {
      forAll (DefaultTimeSeries(Seq()).compute(dates)) { result => result._2 shouldBe None}
   }

   "A default time series with only undefined values" should "generate undefined values" in {
      forAll (DefaultTimeSeries(Seq(u, u)).compute(dates)) { result => result._2 shouldBe None}
   }
}
