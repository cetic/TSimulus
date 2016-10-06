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

import be.cetic.rtsgen.generators.binary.TrueGenerator
import be.cetic.rtsgen.generators.composite.ConditionalGenerator
import be.cetic.rtsgen.generators.primary.ConstantGenerator
import org.scalatest.{FlatSpec, Matchers}

import spray.json._
import be.cetic.rtsgen.config.GeneratorLeafFormat._

class ConditionalGeneratorTest extends FlatSpec with Matchers
{
   val source =
      """
        |{
        |  "name": "conditional-generator",
        |  "type": "conditional",
        |  "condition": { "type": "true" },
        |  "success": { "type": "constant", "value": 17 },
        |  "failure": { "type": "constant", "value": 42 }
        |}
      """.stripMargin

   "A conditional generator" should "be correctly read from a json document" in {
      val document = source.parseJson

      val generator = document.convertTo[ConditionalGenerator]

      generator.name shouldBe Some("conditional-generator")
      generator.condition shouldBe 'right
      generator.success shouldBe Right(new ConstantGenerator(None, 17))
      generator.failure shouldBe Some(Right(new ConstantGenerator(None, 42)))
   }

   it should "be correctly exported to a json document" in {
      val generator = new ConditionalGenerator(
         Some("conditional-generator"),
         Right(new TrueGenerator(None)),
         Right(new ConstantGenerator(None, 17)),
         Some(Right(new ConstantGenerator(None, 42)))
      )
      generator shouldBe generator.toJson.convertTo[ConditionalGenerator]
   }
}