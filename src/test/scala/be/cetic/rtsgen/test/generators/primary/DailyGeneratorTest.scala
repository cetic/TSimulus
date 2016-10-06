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

package be.cetic.rtsgen.test.generators.primary

import be.cetic.rtsgen.config.GeneratorFormat
import org.joda.time.LocalTime
import org.scalatest.{FlatSpec, Matchers}
import spray.json._
import be.cetic.rtsgen.generators.primary.DailyGenerator

class DailyGeneratorTest extends FlatSpec with Matchers
{
   val source =
      """
        |{
        |    "name": "daily-generator",
        |    "type": "daily",
        |    "points": {"11:00:00.000" : 6, "17:00:00.000" : 8, "07:00:00.000" : 2}
        |}
      """.stripMargin

   "A daily generator" should "be correctly read from a json document" in {
      val generator = DailyGenerator(source.parseJson)

      generator.name shouldBe Some("daily-generator")
      generator.`type` shouldBe "daily"
      generator.points shouldBe Map(
         new LocalTime(11,0,0) -> 6,
         new LocalTime(17,0,0) -> 8,
         new LocalTime(7,0,0) -> 2)
   }

   it should "be correctly extracted from the global extractor" in {
      noException should be thrownBy GeneratorFormat.read(source.parseJson)
   }

   it should "be correctly exported to a json document" in {
      val generator = new DailyGenerator(Some("daily-generator"), Map(
         new LocalTime(11,0,0) -> 6,
         new LocalTime(17,0,0) -> 8,
         new LocalTime(7,0,0) -> 2))

      generator shouldBe DailyGenerator(generator.toJson)
   }
}
