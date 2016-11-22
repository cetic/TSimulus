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

package be.cetic.rtsgen.test.generators.binary

import be.cetic.rtsgen.config.GeneratorFormat
import org.scalatest.{FlatSpec, Inspectors, Matchers}
import spray.json._
import be.cetic.rtsgen.generators.binary.ImpliesGenerator
import be.cetic.rtsgen.test.RTSTest

class ImpliesGeneratorTest extends FlatSpec with Matchers with Inspectors with RTSTest
{
   val source =
      """
        |{
        |   "name": "implies-generator",
        |   "type": "implies",
        |   "a": "daily-generator",
        |   "b": "monthly-generator"
        |}
      """.stripMargin

   "An Implies generator" should "be correctly read from a json document" in {
      val generator = ImpliesGenerator(source.parseJson)

      generator.name shouldBe Some("implies-generator")
      generator.a shouldBe Left("daily-generator")
      generator.b shouldBe Left("monthly-generator")
   }

   it should "be correctly extracted from the global extractor" in {
      noException should be thrownBy GeneratorFormat.read(source.parseJson)
   }

   it should "be correctly exported to a json document" in {
      val generator = new ImpliesGenerator(
         Some("implies-generator"),
         Left("daily-generator"),
         Left("monthly-generator")
      )
      generator shouldBe ImpliesGenerator(generator.toJson)
   }
}
