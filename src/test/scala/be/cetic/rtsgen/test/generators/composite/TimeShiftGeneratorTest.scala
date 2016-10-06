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

package be.cetic.rtsgen.test.generators.composite

import org.joda.time.Duration
import spray.json._
import be.cetic.rtsgen.config.GeneratorLeafFormat._
import be.cetic.rtsgen.generators.composite.TimeShiftGenerator
import org.scalatest.{FlatSpec, Matchers}

class TimeShiftGeneratorTest extends FlatSpec with Matchers
{
   val timeShiftedSource =
      """
        |{
        |   "name": "time-shifted-generator",
        |   "type": "time-shift",
        |   "generator": "daily-generator",
        |   "shift": -8000
        |}
      """.stripMargin

   "A time shifted generator" should "be correctly read from a json document" in {
      val document = timeShiftedSource.parseJson

      val generator = document.convertTo[TimeShiftGenerator]

      generator.name shouldBe Some("time-shifted-generator")
      generator.generator shouldBe Left("daily-generator")
      generator.shift shouldBe new Duration(-8000)
   }

   it should "be correctly exported to a json document" in {
      val generator = new TimeShiftGenerator(
         Some("time-shifted-generator"),
         Left("daily-generator"),
         new Duration(-8000)
      )
      generator shouldBe generator.toJson.convertTo[TimeShiftGenerator]
   }
}
