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

package be.cetic.tsimulus.test.timeseries.primary

import be.cetic.tsimulus.Utils
import be.cetic.tsimulus.test.RTSTest
import be.cetic.tsimulus.timeseries.primary.SinusTimeSeries
import org.joda.time.LocalDateTime
import org.scalatest.{FlatSpec, Inspectors, Matchers}

class SinusSeriesTest extends FlatSpec with Matchers with Inspectors with RTSTest
{
   "A sinus series" should "provide a value close to 0 for its origin" in
   {
      val time = new LocalDateTime(2020, 6, 7, 1, 2, 3)
      val ts = SinusTimeSeries(time, 1000)
      val value = ts.compute(time).get

      value shouldBe (0.0 +- 0.0001)
   }

   it should "provide a value close to 0 for its origin + 1 period" in
   {
      val time = new LocalDateTime(2020, 6, 7, 1, 2, 3)
      val ts = SinusTimeSeries(time, 1000)
      val value = ts.compute(time.plusMillis(1000)).get

      value shouldBe (0.0 +- 0.0001)
   }

   it should "provide a value close to 0 for its origin - 1 period" in
   {
     val time = new LocalDateTime(2020, 6, 7, 1, 2, 3)
     val ts = SinusTimeSeries(time, 1000)
     val value = ts.compute(time.plusMillis(-1000)).get

     value shouldBe (0.0 +- 0.0001)
   }

   it should "provide a value close to 0 for its origin + half its period" in
   {
      val time = new LocalDateTime(2020, 6, 7, 1, 2, 3)
      val ts = SinusTimeSeries(time, 1000)
      val value = ts.compute(time.plusMillis(500)).get

      value shouldBe (0.0 +- 0.0001)
   }

   it should "provide a value close to 1 for its origin + 1/4 its period" in
   {
      val time = new LocalDateTime(2020, 6, 7, 1, 2, 3)
      val ts = SinusTimeSeries(time, 1000)
      val value = ts.compute(time.plusMillis(250)).get

      value shouldBe (1.0 +- 0.0001)
   }

   it should "provide a value close to 0 for its origin - half its period" in
   {
      val time = new LocalDateTime(2020, 6, 7, 1, 2, 3)
      val ts = SinusTimeSeries(time, 1000)
      val value = ts.compute(time.plusMillis(-500)).get

      value shouldBe (0.0 +- 0.0001)
   }

   it should "provide a value close to -1 for its origin - 1/4 its period" in
   {
      val time = new LocalDateTime(2020, 6, 7, 1, 2, 3)
      val ts = SinusTimeSeries(time, 1000)
      val value = ts.compute(time.plusMillis(-250)).get

      value shouldBe (-1.0 +- 0.0001)
   }
}
