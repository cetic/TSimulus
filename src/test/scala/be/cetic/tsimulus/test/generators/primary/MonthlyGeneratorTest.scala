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

import be.cetic.tsimulus.config.GeneratorFormat
import org.scalatest.{FlatSpec, Matchers}
import spray.json._
import be.cetic.tsimulus.generators.primary.MonthlyGenerator

class MonthlyGeneratorTest extends FlatSpec with Matchers
{
   val source =
      """
        |{
        |   "name": "monthly-generator",
        |   "type": "monthly",
        |   "points":  {"january": -6.3, "february": -6.9, "june" : -2.7}
        |}
      """.stripMargin

   "A monthly generator" should "be correctly read from a json document" in {
      val generator = MonthlyGenerator(source.parseJson)

      generator.name shouldBe Some("monthly-generator")
      generator.`type` shouldBe "monthly"
      generator.points shouldBe Map(
         "january" -> -6.3,
         "february" -> -6.9,
         "june" -> -2.7
      )
   }

   it should "be correctly extracted from the global extractor" in {
      noException should be thrownBy GeneratorFormat.read(source.parseJson)
   }

   it should "be correctly exported to a json document" in {
      val generator = new MonthlyGenerator(Some("monthly-generator"), Map(
         "january" -> -6.3,
         "february" -> -6.9,
         "june" -> -2.7
      ))

      generator shouldBe MonthlyGenerator(generator.toJson)
   }

   "A monthly generator with all months" should "correctly be parsed" in {
      val sourceComplete =
      """
        |{
        |      "name": "monthly-generator",
        |      "type": "monthly",
        |      "points": {"january": 3.3, "february": 3.7, "march": 6.8, "april": 9.8, "may": 13.6, "june": 16.2,
        |        "july": 18.4, "august": 18, "september": 14.9, "october": 11.1, "november": 6.8, "december": 3.9}
        |}
      """.stripMargin

      val generator = MonthlyGenerator(sourceComplete.parseJson)

      generator.name shouldBe Some("monthly-generator")
      generator.`type` shouldBe "monthly"
      generator.points shouldBe Map(
         "january" -> 3.3,
         "february" -> 3.7,
         "march" -> 6.8,
         "april" -> 9.8,
         "may" -> 13.6,
         "june" -> 16.2,
         "july" -> 18.4,
         "august" -> 18,
         "september" -> 14.9,
         "october" -> 11.1,
         "november" -> 6.8,
         "december" -> 3.9
      )
   }
}
