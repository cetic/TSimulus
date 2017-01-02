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

package be.cetic.tsimulus.test.generators.composite

import be.cetic.tsimulus.config.GeneratorFormat
import org.scalatest.{FlatSpec, Matchers}
import spray.json._
import be.cetic.tsimulus.generators.composite.CorrelatedGenerator

class CorrelatedGeneratorTest extends FlatSpec with Matchers
{
   val source =
      """
        |{
        |   "name": "corr-generator",
        |   "type": "correlated",
        |   "generator": "daily-generator",
        |   "coef": 0.8
        |}
      """.stripMargin

   "A correlated generator" should "be correctly read from a json document" in {
      val generator = CorrelatedGenerator(source.parseJson)

      generator.name shouldBe Some("corr-generator")
      generator.generator shouldBe Left("daily-generator")
      generator.coef shouldBe 0.8
   }

   it should "be correctly extracted from the global extractor" in {
      noException should be thrownBy GeneratorFormat.read(source.parseJson)
   }

   it should "be correctly exported to a json document" in {
      val generator = new CorrelatedGenerator(
         Some("corr-generator"),
         Left("daily-generator"),
         0.8
      )
      generator shouldBe CorrelatedGenerator(generator.toJson)
   }

   it should "have a correct textual representation" in {
      val generator = new CorrelatedGenerator(
         Some("corr-generator"),
         Left("daily-generator"),
         0.8
      )

      generator.toString shouldBe """Correlated(Some(corr-generator), Left(daily-generator), 0.8)"""
   }
}
