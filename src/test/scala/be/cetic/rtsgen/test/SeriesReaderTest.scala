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

package be.cetic.rtsgen.test

import be.cetic.rtsgen.config.Series
import org.joda.time.Duration
import org.scalatest.{FlatSpec, Matchers}
import spray.json._

class SeriesReaderTest extends FlatSpec with Matchers
{
   val seriesSource =
      """
        |{
        |   "name": "myName",
        |   "generator": "daily-generator",
        |   "frequency": 60000
        |}
      """.stripMargin

   "A series" should "be correctly read from a json document" in {
      val document = seriesSource.parseJson

      val series = Series(document)

      series.name shouldBe "myName"
      series.generator shouldBe Left("daily-generator")
      series.frequency shouldBe new Duration(60000)
   }

   it should "be correctly exported to a json document" in {
      val series = Series[Any](
         "myName",
         Left("daily-generator"),
         new Duration(60000)
      )
      series shouldBe Series(series.toJson)
   }
}
