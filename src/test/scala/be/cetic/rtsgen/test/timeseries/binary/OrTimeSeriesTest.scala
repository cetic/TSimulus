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
import be.cetic.rtsgen.timeseries.binary.OrTimeSeries
import org.scalatest.{FlatSpec, Inspectors, Matchers}


class OrTimeSeriesTest extends FlatSpec
               with Matchers
               with Inspectors
               with RTSTest
{
   "True OR True" should "be True" in {
      forAll (OrTimeSeries(t, t).compute(dates)) { result => result._2 shouldBe Some(true)}
   }

   "True OR False" should "be True" in {
      forAll (OrTimeSeries(t, f).compute(dates)) { result => result._2 shouldBe Some(true)}
   }

   "False OR True" should "be True" in {
      forAll (OrTimeSeries(f, t).compute(dates)) { result => result._2 shouldBe Some(true)}
   }

   "False OR False" should "be False" in {
      forAll (OrTimeSeries(f, f).compute(dates)) { result => result._2 shouldBe Some(false)}
   }

   "True OR Undetermined" should "be Undetermined" in {
      forAll (OrTimeSeries(t, u).compute(dates)) { result => result._2 shouldBe None}
   }

   "False OR Undetermined" should "be Undetermined" in {
      forAll (OrTimeSeries(f, u).compute(dates)) { result => result._2 shouldBe None}
   }

   "Undetermined OR True" should "be Undetermined" in {
      forAll (OrTimeSeries(u, t).compute(dates)) { result => result._2 shouldBe None}
   }

   "Undetermined OR False" should "be Undetermined" in {
      forAll (OrTimeSeries(u, f).compute(dates)) { result => result._2 shouldBe None}
   }

   "Undetermined OR Undetermined" should "be Undetermined" in {
      forAll (OrTimeSeries(u, u).compute(dates)) { result => result._2 shouldBe None}
   }
}
