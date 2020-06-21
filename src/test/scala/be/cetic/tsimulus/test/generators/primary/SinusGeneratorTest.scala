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
import be.cetic.tsimulus.generators.primary.{ConstantGenerator, SinusGenerator}
import org.joda.time.LocalDateTime
import org.scalatest.{FlatSpec, Matchers}
import spray.json._

class SinusGeneratorTest extends FlatSpec with Matchers
{
   val source =
      """
        |{
        |   "name": "sinus-generator",
        |   "type": "sinus",
        |   "origin": "2020-06-07 01:02:03",
        |   "period": 1000
        |}
      """.stripMargin

   "A sinus generator" should "be correctly read from a json document" in {
      val generator = SinusGenerator(source.parseJson)

      generator.name shouldBe Some("sinus-generator")
      generator.`type` shouldBe "sinus"
      generator.origin shouldBe new LocalDateTime(2020, 6, 7, 1, 2, 3)
   }

   it should "be extracted from the global extractor without any error" in {
      noException should be thrownBy GeneratorFormat.read(source.parseJson)
   }

   it should "be correctly extracted from the global extractor" in {
      GeneratorFormat.read(source.parseJson) shouldBe SinusGenerator(source.parseJson)
   }


   it should "be correctly exported to a json document" in {
      val generator = new SinusGenerator(Some("sinus-generator"), new LocalDateTime(2020, 6, 7, 1, 2, 3), 1000)
      generator shouldBe SinusGenerator(generator.toJson)
   }
}

