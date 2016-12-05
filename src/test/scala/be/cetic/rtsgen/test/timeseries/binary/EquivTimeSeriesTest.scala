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
import be.cetic.rtsgen.timeseries.binary.{AndTimeSeries, EquivTimeSeries}
import org.scalatest.{FlatSpec, Inspectors, Matchers}

class EquivTimeSeriesTest extends FlatSpec with Matchers with Inspectors with RTSTest
{
   "True Equiv True" should "be True" in {
      forAll (new EquivTimeSeries(t, t).compute(dates)) { result => result._2 shouldBe Some(true)}
   }

   "True Equiv False" should "be False" in {
      forAll (new EquivTimeSeries(t, f).compute(dates)) { result => result._2 shouldBe Some(false)}
   }

   "False Equiv True" should "be False" in {
      forAll (new EquivTimeSeries(f, t).compute(dates)) { result => result._2 shouldBe Some(false)}
   }

   "False Equiv False" should "be TRUE" in {
      forAll (new EquivTimeSeries(f, f).compute(dates)) { result => result._2 shouldBe Some(true)}
   }

   "True Equiv Undefined" should "be Undefined" in {
      forAll (new EquivTimeSeries(t, u).compute(dates)) { result => result._2 shouldBe None}
   }

   "Undefined Equiv True" should "be Undefined" in {
      forAll (new EquivTimeSeries(u, t).compute(dates)) { result => result._2 shouldBe None}
   }

   "Undefined Equiv Undefined" should "be Undefined" in {
      forAll (new EquivTimeSeries(u, u).compute(dates)) { result => result._2 shouldBe None}
   }

   "False Equiv Undefined" should "be Undefined" in {
      forAll (new EquivTimeSeries(f, u).compute(dates)) { result => result._2 shouldBe None}
   }

   "Undefined Equiv False" should "be Undefined" in {
      forAll (new EquivTimeSeries(u, f).compute(dates)) { result => result._2 shouldBe None}
   }
}
