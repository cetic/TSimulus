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

package be.cetic.tsimulus.test.timeseries.binary

import be.cetic.tsimulus.test.RTSTest
import be.cetic.tsimulus.timeseries.binary.ImpliesTimeSeries
import org.scalatest.{FlatSpec, Inspectors, Matchers}

class ImpliesTimeSeriesTest extends FlatSpec with Matchers with Inspectors with RTSTest
{
   "True IMPLIES True" should "be True" in {
      forAll (new ImpliesTimeSeries(t, t).compute(dates)) { result => result._2 shouldBe Some(true)}
   }

   "True IMPLIES False" should "be False" in {
      forAll (new ImpliesTimeSeries(t, f).compute(dates)) { result => result._2 shouldBe Some(false)}
   }

   "False IMPLIES True" should "be True" in {
      forAll (new ImpliesTimeSeries(f, t).compute(dates)) { result => result._2 shouldBe Some(true)}
   }

   "False IMPLIES False" should "be TRUE" in {
      forAll (new ImpliesTimeSeries(f, f).compute(dates)) { result => result._2 shouldBe Some(true)}
   }

   "True IMPLIES Undefined" should "be Undefined" in {
      forAll (new ImpliesTimeSeries(t, u).compute(dates)) { result => result._2 shouldBe None}
   }

   "Undefined IMPLIES True" should "be Undefined" in {
      forAll (new ImpliesTimeSeries(u, t).compute(dates)) { result => result._2 shouldBe None}
   }

   "Undefined IMPLIES Undefined" should "be Undefined" in {
      forAll (new ImpliesTimeSeries(u, u).compute(dates)) { result => result._2 shouldBe None}
   }

   "False IMPLIES Undefined" should "be Undefined" in {
      forAll (new ImpliesTimeSeries(f, u).compute(dates)) { result => result._2 shouldBe None}
   }

   "Undefined IMPLIES False" should "be Undefined" in {
      forAll (new ImpliesTimeSeries(u, f).compute(dates)) { result => result._2 shouldBe None}
   }
}
