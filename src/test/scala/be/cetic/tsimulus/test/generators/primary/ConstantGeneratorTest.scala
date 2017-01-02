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

package be.cetic.tsimulus.test.generators.primary

import be.cetic.tsimulus.config.GeneratorFormat
import org.scalatest.{FlatSpec, Matchers}
import spray.json._
import be.cetic.tsimulus.generators.primary.ConstantGenerator

class ConstantGeneratorTest extends FlatSpec with Matchers
{
   val source =
      """
        |{
        |   "name": "constant-generator",
        |   "type": "constant",
        |   "value":  17.5
        |}
      """.stripMargin

   "A constant generator" should "be correctly read from a json document" in {
      val generator = ConstantGenerator(source.parseJson)

      generator.name shouldBe Some("constant-generator")
      generator.`type` shouldBe "constant"
      generator.value shouldBe 17.5
   }

   it should "be correctly extracted from the global extractor" in {
      noException should be thrownBy GeneratorFormat.read(source.parseJson)
   }

   it should "be correctly exported to a json document" in {
      val generator = new ConstantGenerator(Some("constant-generator"), 17.5)
      generator shouldBe ConstantGenerator(generator.toJson)
   }
}
