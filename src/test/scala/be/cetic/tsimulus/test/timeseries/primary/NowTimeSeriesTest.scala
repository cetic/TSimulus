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

import be.cetic.tsimulus.test.RTSTest
import be.cetic.tsimulus.timeseries.primary.NowTimeSeries
import org.scalatest.{FlatSpec, Inspectors, Matchers}

class NowTimeSeriesTest extends FlatSpec with Matchers with Inspectors with RTSTest
{
   "A Now time series" should "provide as a value the proposed timestamp" in {
      forAll (NowTimeSeries().compute(dates)) { result => result._2 shouldBe Some(result._1) }
   }

}
