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
import be.cetic.tsimulus.generators.primary.NowGenerator
import org.scalatest.{FlatSpec, Matchers}
import spray.json._


class NowGeneratorTest extends FlatSpec with Matchers
{
   val source = """
                  |{
                  |  "name": "g1",
                  |  "type": "now"
                  |}
                """.stripMargin

   "A Now generator" should "be correctly read from a json document" in {
      val generator = NowGenerator(source.parseJson)

      generator.name shouldBe Some("g1")
      generator.`type` shouldBe "now"
   }

   it should "be extracted from the global extractor without any error" in {
      noException should be thrownBy GeneratorFormat.read(source.parseJson)
   }

   it should "be correctly extracted from the global extractor" in {
      GeneratorFormat.read(source.parseJson) shouldBe NowGenerator(source.parseJson)
   }

   it should "be correctly exported to a json document" in {
      val generator = new NowGenerator(Some("g1"))
      generator shouldBe NowGenerator(generator.toJson)
   }
}

