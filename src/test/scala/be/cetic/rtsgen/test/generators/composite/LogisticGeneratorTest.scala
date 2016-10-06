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

import org.scalatest.{FlatSpec, Inspectors, Matchers}
import spray.json._
import be.cetic.rtsgen.config.GeneratorLeafFormat._
import be.cetic.rtsgen.generators.binary.LogisticGenerator

class LogisticGeneratorTest extends FlatSpec with Matchers with Inspectors
{
   val logisticSource =
      """
        |{
        |   "name": "logistic-generator",
        |   "type": "logistic",
        |   "generator": "daily-generator",
        |   "location": 6,
        |   "scale": 2.4,
        |   "seed": 1809
        |}
      """.stripMargin

   "A logistic generator" should "be correctly read from a json document" in {
      val document = logisticSource.parseJson

      val generator = document.convertTo[LogisticGenerator]

      generator.name shouldBe Some("logistic-generator")
      generator.generator shouldBe Left("daily-generator")
      generator.location shouldBe 6
      generator.scale shouldBe 2.4
      generator.seed shouldBe Some(1809)
   }

   it should "be correctly exported to a json document" in {
      val generator = new LogisticGenerator(
         Some("logistic-generator"),
         Left("daily-generator"),
         6,
         2.4,
         Some(1809)
      )
      generator shouldBe generator.toJson.convertTo[LogisticGenerator]
   }
}
