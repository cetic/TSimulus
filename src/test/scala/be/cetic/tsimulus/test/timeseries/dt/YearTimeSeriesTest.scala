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

package be.cetic.tsimulus.test.timeseries.dt

import be.cetic.tsimulus.test.RTSTest
import be.cetic.tsimulus.timeseries.dt.YearTimeSeries
import be.cetic.tsimulus.timeseries.primary.NowTimeSeries
import org.scalatest.{FlatSpec, Inspectors, Matchers}

class YearTimeSeriesTest extends FlatSpec with Matchers with Inspectors with RTSTest
{
   "A Year time series" should "produce the year of the underlying timestamp" in
      {
         (new YearTimeSeries(NowTimeSeries()).compute(fixedDate)) match
         {
            case Some(x) => x == 2019
            case _ => false
         }
      }
}
