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

import org.scalatest.{FlatSpec, Matchers}
import spray.json._
import be.cetic.rtsgen.config.GeneratorLeafFormat._
import be.cetic.rtsgen.generators.MonthlyGenerator

class MonthlyGeneratorTest extends FlatSpec with Matchers
{
   val monthlySource =
      """
        |{
        |   "name": "monthly-generator",
        |   "type": "monthly",
        |   "points":  {"january": -6.3, "february": -6.9, "june" : -2.7}
        |}
      """.stripMargin

   "A monthly generator" should "be correctly read from a json document" in {
      val document = monthlySource.parseJson

      val generator = document.convertTo[MonthlyGenerator]

      generator.name shouldBe Some("monthly-generator")
      generator.`type` shouldBe "monthly"
      generator.points shouldBe Map(
         "january" -> -6.3,
         "february" -> -6.9,
         "june" -> -2.7
      )
   }

   it should "be correctly exported to a json document" in {
      val generator = new MonthlyGenerator(Some("monthly-generator"), Map(
         "january" -> -6.3,
         "february" -> -6.9,
         "june" -> -2.7
      ))

      generator shouldBe generator.toJson.convertTo[MonthlyGenerator]
   }

}
