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
import be.cetic.rtsgen.generators.composite.FunctionGenerator
import be.cetic.rtsgen.generators.primary.ConstantGenerator

class FunctionGeneratorTest extends FlatSpec with Matchers
{
   val source =
      """
        |{
        |   "name": "function-generator",
        |   "type": "function",
        |   "generator": { "type" : "constant", "value" : 42 },
        |   "slope": 1.4,
        |   "intercept" : 9.2
        |}
      """.stripMargin

   "A function generator" should "be correctly read from a json document" in {
      val generator = FunctionGenerator(source.parseJson)

      generator.name shouldBe Some("function-generator")
      generator.`type` shouldBe "function"
      generator.slope shouldBe 1.4
      generator.intercept shouldBe 9.2
      generator.generator shouldBe Right(new ConstantGenerator(None, 42))
   }

   it should "be correctly extracted from the global extractor" in {
      noException should be thrownBy GeneratorFormat.read(source.parseJson)
   }

   it should "be correctly exported to a json document" in {
      val generator = new FunctionGenerator(
         Some("function-generator"),
         Right(new ConstantGenerator(None, 42)),
         1.4,
         9.2
      )

      generator shouldBe FunctionGenerator(generator.toJson)
   }
}
