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

package be.cetic.tsimulus.test.generators.binary

import be.cetic.tsimulus.config.GeneratorFormat
import be.cetic.tsimulus.generators.binary.LogisticGenerator
import org.scalatest.{FlatSpec, Inspectors, Matchers}
import spray.json._
import be.cetic.tsimulus.test.RTSTest

class LogisticGeneratorTest extends FlatSpec with Matchers with Inspectors with RTSTest
{
   val source =
      """
        |{
        |  "name": "logistic-generator",
        |  "type": "logistic",
        |  "generator": "g1",
        |  "location": 6,
        |  "scale": 2.4,
        |  "seed": 1809
        |}
      """.stripMargin

   val generator = LogisticGenerator(source.parseJson)

   "A logistic generator" should "be correctly read from a json document" in {

      generator.name shouldBe Some("logistic-generator")
      generator.generator shouldBe Left("g1")
      generator.location shouldBe 6
      generator.scale shouldBe 2.4 +- 0.001
      generator.seed shouldBe Some(1809)
   }

   it should "be correctly extracted from the global extractor" in {
      noException should be thrownBy GeneratorFormat.read(source.parseJson)
   }

   it should "be correctly exported to a json document" in {
      val generator = new LogisticGenerator(
         Some("logistic-generator"),
         Left("g1"),
         6,
         2.4,
         Some(1809)
      )
      generator shouldBe LogisticGenerator(generator.toJson)
   }

   it should "have a correct textual representation" in {
      val generator = new LogisticGenerator(
         Some("logistic-generator"),
         Left("g1"),
         6,
         2.4,
         Some(1809)
      )

      generator.toString shouldBe """Logistic(Some(logistic-generator), Left(g1), 6.0, 2.4, Some(1809))"""
   }
}
