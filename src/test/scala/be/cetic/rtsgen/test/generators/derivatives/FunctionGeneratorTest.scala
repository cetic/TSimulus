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

import be.cetic.rtsgen.config.{ConstantGenerator, FunctionGenerator}
import org.scalatest.{FlatSpec, Matchers}
import spray.json._
import be.cetic.rtsgen.config.GeneratorLeafFormat._

class FunctionGeneratorTest extends FlatSpec with Matchers
{
   val functionSource =
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
      val document = functionSource.parseJson

      val generator = document.convertTo[FunctionGenerator]

      generator.name shouldBe Some("function-generator")
      generator.`type` shouldBe "function"
      generator.slope shouldBe 1.4
      generator.intercept shouldBe 9.2
      generator.generator shouldBe Right(new ConstantGenerator(None, 42))
   }

   it should "be correctly exported to a json document" in {
      val generator = new FunctionGenerator(
         Some("function-generator"),
         Right(new ConstantGenerator(None, 42)),
         1.4,
         9.2
      )
      val a = generator.toJson

      generator shouldBe a.convertTo[FunctionGenerator]
   }
}