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

package be.cetic.rtsgen.test.generators.derivatives

import be.cetic.rtsgen.config.ThresholdGenerator
import org.scalatest.{FlatSpec, Matchers}
import spray.json._
import be.cetic.rtsgen.config.GeneratorLeafFormat._

class ThresholdGeneratorTest extends FlatSpec with Matchers
{
   val thresholdSource =
      """
        |{
        |   "name": "threshold-generator",
        |   "type": "threshold",
        |   "generator": "daily-generator",
        |   "threshold": 42,
        |   "included": true
        |}
      """.stripMargin

   "A threshold generator" should "be correctly read from a json document" in {
      val document = thresholdSource.parseJson

      val generator = document.convertTo[ThresholdGenerator]

      generator.name shouldBe Some("threshold-generator")
      generator.generator shouldBe Left("daily-generator")
      generator.threshold shouldBe 42
      generator.included shouldBe Some(true)
   }

   it should "be correctly exported to a json document" in {
      val generator = new ThresholdGenerator(
         Some("threshold-generator"),
         Left("daily-generator"),
         42,
         Some(false)
      )
      generator shouldBe generator.toJson.convertTo[ThresholdGenerator]
   }
}
