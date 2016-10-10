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

package be.cetic.rtsgen.test.generators.missing

import be.cetic.rtsgen.config.GeneratorFormat
import be.cetic.rtsgen.generators.missing.DefaultGenerator
import org.scalatest.{FlatSpec, Inspectors, Matchers}
import spray.json._

class DefaultGeneratorTest extends FlatSpec with Matchers with Inspectors
{
   val source =
      """
        |{
        |  "name": "default-generator",
        |  "type": "first-of",
        |  "generators": ["daily-generator", "random-generator"]
        |}
      """.stripMargin

   "A Default generator" should "be correctly read from a json document" in {
      val generator = DefaultGenerator(source.parseJson)

      generator.name shouldBe Some("default-generator")
      generator.gens shouldBe Seq(Left("daily-generator"), Left("random-generator"))
   }

   it should "be correctly extracted from the global extractor" in {
      noException should be thrownBy GeneratorFormat.read(source.parseJson)
   }

   it should "be correctly exported to a json document" in {
      val generator = new DefaultGenerator(
         Some("default-generator"),
         Seq(Left("daily-generator"), Left("random-generator"))
      )
      println(generator.toJson)
      generator shouldBe DefaultGenerator(generator.toJson)
   }
}
