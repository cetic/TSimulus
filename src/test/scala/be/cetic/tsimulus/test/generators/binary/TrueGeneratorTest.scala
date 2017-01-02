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
import be.cetic.tsimulus.generators.binary.TrueGenerator
import org.scalatest.{FlatSpec, Inspectors, Matchers}
import spray.json._

class TrueGeneratorTest extends FlatSpec with Matchers with Inspectors
{
   val source =
      """
        |{
        |   "name": "true-generator",
        |   "type": "true"
        |}
      """.stripMargin

   "A TRUE generator" should "be correctly read from a json document" in {
      val generator = TrueGenerator(source.parseJson)

      generator.name shouldBe Some("true-generator")
   }

   it should "be correctly extracted from the global extractor" in {
      noException should be thrownBy GeneratorFormat.read(source.parseJson)
   }

   it should "be correctly exported to a json document" in {
      val generator = new TrueGenerator(
         Some("true-generator")
      )
      generator shouldBe TrueGenerator(generator.toJson)
   }

   it should "have a correct textual representation" in {
      val generator = new TrueGenerator(
         Some("true-generator")
      )

      generator.toString shouldBe """True(Some(true-generator))"""
   }
}
