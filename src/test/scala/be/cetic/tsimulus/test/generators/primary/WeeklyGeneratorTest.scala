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

package be.cetic.tsimulus.test.generators.primary

import java.security.InvalidParameterException

import be.cetic.tsimulus.config.GeneratorFormat
import org.scalatest.{FlatSpec, Matchers}
import spray.json._
import be.cetic.tsimulus.generators.primary.WeeklyGenerator

class WeeklyGeneratorTest extends FlatSpec with Matchers
{
   val source =
      """
        |{
        |   "name": "weekly-generator",
        |   "type": "weekly",
        |   "points": {"monday": 8.7, "friday": -3.6, "sunday" : 10.9}
        |}
      """.stripMargin

   "A weekly generator" should "be correctly read from a json document" in {
      val generator = WeeklyGenerator(source.parseJson)

      generator.name shouldBe Some("weekly-generator")
      generator.`type` shouldBe "weekly"
      generator.points shouldBe Map(
         "monday" -> 8.7,
         "friday" -> -3.6,
         "sunday" -> 10.9)
   }

   it should "be extracted from the global extractor without any error" in {
      noException should be thrownBy GeneratorFormat.read(source.parseJson)
   }

   it should "be correctly extracted from the global extractor" in {
      GeneratorFormat.read(source.parseJson) shouldBe WeeklyGenerator(source.parseJson)
   }


   it should "be correctly exported to a json document" in {
      val generator = new WeeklyGenerator(Some("weekly-generator"), Map(
         "monday" -> 8.7,
         "friday" -> -3.6,
         "sunday" -> 10.9))

      generator shouldBe WeeklyGenerator(generator.toJson)
   }

   it should "support all the days of the week" in {
      val data = """
        |{
        |   "name": "weekly-generator",
        |   "type": "weekly",
        |   "points": {"monday": 8.7, "tuesday": 1.3, "wednesday": 5.7, "thursday": 8.4, "friday": -3.6, "saturday": 9.52, "sunday" : 10.9}
        |}
      """.stripMargin

      noException should be thrownBy WeeklyGenerator(data.parseJson)
   }

   it should "reject invalid day names" in {
      val data = """
                   |{
                   |   "name": "weekly-generator",
                   |   "type": "weekly",
                   |   "points": {"X": 8.7}
                   |}
                 """.stripMargin

      an [InvalidParameterException] should be thrownBy WeeklyGenerator(data.parseJson)
   }

}