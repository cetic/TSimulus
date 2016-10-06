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
import be.cetic.rtsgen.timeseries.binary.GreaterThanTimeSeries
import be.cetic.rtsgen.timeseries.primary.{ConstantTimeSeries, UndefinedTimeSeries}
import org.scalatest.{FlatSpec, Inspectors, Matchers}

class GreaterThanTimeSeriesTest extends FlatSpec with Matchers with Inspectors with RTSTest
{
   "A greater than time series for which A is always greater than B" should "produce true" in {
      forAll (new GreaterThanTimeSeries(ConstantTimeSeries(2), ConstantTimeSeries(1)).compute(dates)) {
         _._2 shouldBe Some(true)
      }
   }

   "A greater than time series for which A is always lesser than B" should "produce false" in {
      forAll (new GreaterThanTimeSeries(ConstantTimeSeries(1), ConstantTimeSeries(2)).compute(dates)) {
         _._2 shouldBe Some(false)
      }
   }

   "A greater than time series for which A is always undefined" should "produce undefined values" in {
      forAll (new GreaterThanTimeSeries(new UndefinedTimeSeries(), ConstantTimeSeries(1)).compute(dates)) {
         _._2 shouldBe None
      }
   }

   "A greater than time series for which B is always undefined" should "produce undefined values" in {
      forAll (new GreaterThanTimeSeries(ConstantTimeSeries(1), new UndefinedTimeSeries()).compute(dates)) {
         _._2 shouldBe None
      }
   }

   "A greater than time series for which A and B are always undefined" should "produce undefined values" in {
      forAll (new GreaterThanTimeSeries(new UndefinedTimeSeries(), new UndefinedTimeSeries()).compute(dates)) {
         _._2 shouldBe None
      }
   }
}
