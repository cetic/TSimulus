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
import be.cetic.rtsgen.timeseries.binary.AndTimeSeries
import org.scalatest.{FlatSpec, Inspectors, Matchers}

class AndTimeSeriesTest extends FlatSpec with Matchers
                                            with Inspectors
                                            with RTSTest
{
   "True AND True" should "be True" in {
      forAll (new AndTimeSeries(t, t).compute(dates)) { result => result._2 shouldBe Some(true)}
   }

   "True AND False" should "be False" in {
      forAll (new AndTimeSeries(t, f).compute(dates)) { result => result._2 shouldBe Some(false)}
   }

   "False AND True" should "be False" in {
      forAll (new AndTimeSeries(f, t).compute(dates)) { result => result._2 shouldBe Some(false)}
   }

   "False AND False" should "be False" in {
      forAll (new AndTimeSeries(f, f).compute(dates)) { result => result._2 shouldBe Some(false)}
   }

   "True AND Undefined" should "be Undefined" in {
      forAll (new AndTimeSeries(t, u).compute(dates)) { result => result._2 shouldBe None}
   }

   "Undefined AND True" should "be Undefined" in {
      forAll (new AndTimeSeries(u, t).compute(dates)) { result => result._2 shouldBe None}
   }

   "Undefined AND Undefined" should "be Undefined" in {
      forAll (new AndTimeSeries(u, u).compute(dates)) { result => result._2 shouldBe None}
   }

   "False AND Undefined" should "be Undefined" in {
      forAll (new AndTimeSeries(f, u).compute(dates)) { result => result._2 shouldBe None}
   }

   "Undefined AND False" should "be Undefined" in {
      forAll (new AndTimeSeries(u, f).compute(dates)) { result => result._2 shouldBe None}
   }
}
