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

import be.cetic.rtsgen.config.GeneratorFormat
import org.scalatest.{FlatSpec, Matchers}
import spray.json._
import be.cetic.rtsgen.generators.composite.DivideGenerator

class DivideGeneratorTest extends FlatSpec with Matchers
{
   val source =
      """
        |{
        |   "name": "divide-generator",
        |   "type": "divide",
        |   "numerator": "num-generator",
        |   "denominator": "den-generator"
        |}
      """.stripMargin

   "A divide generator" should "be correctly read from a json document" in {
      val generator = DivideGenerator(source.parseJson)

      generator.name shouldBe Some("divide-generator")
      generator.numerator shouldBe Left("num-generator")
      generator.denominator shouldBe Left("den-generator")
   }

   it should "be correctly extracted from the global extractor" in {
      noException should be thrownBy GeneratorFormat.read(source.parseJson)
   }

   it should "be correctly exported to a json document" in {
      val generator = new DivideGenerator(
         Some("divide-generator"),
         Left("daily-generator"),
         Left("daily-generator")
      )
      generator shouldBe DivideGenerator(generator.toJson)
   }
}
