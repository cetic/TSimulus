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

package be.cetic.tsimulus.test.generators.dt

import be.cetic.tsimulus.config.GeneratorFormat
import be.cetic.tsimulus.generators.dt.DateTimeDifferenceGenerator
import be.cetic.tsimulus.generators.primary.NowGenerator
import org.scalatest.{FlatSpec, Matchers}
import spray.json._


class DateTimeDifferenceGeneratorTest extends FlatSpec with Matchers
{
   val source = """
                  |{
                  |  "name": "g1",
                  |  "type": "dt::diff",
                  |  "a": {"type": "now"},
                  |  "b": {"type": "now"}
                  |}
                """.stripMargin

   "A DateTimeDifference generator" should "be correctly read from a json document" in {
      val generator = DateTimeDifferenceGenerator(source.parseJson)

      generator.name shouldBe Some("g1")
      generator.`type` shouldBe "dt::diff"
      generator.a shouldBe Right(NowGenerator())
      generator.b shouldBe Right(NowGenerator())
   }

   it should "be extracted from the global extractor without any error" in {
      noException should be thrownBy GeneratorFormat.read(source.parseJson)
   }

   it should "be correctly extracted from the global extractor" in {
      GeneratorFormat.read(source.parseJson) shouldBe DateTimeDifferenceGenerator(source.parseJson)
   }

   it should "be correctly exported to a json document" in {
      val generator = DateTimeDifferenceGenerator(Some("g1"), NowGenerator(), NowGenerator())
      generator shouldBe DateTimeDifferenceGenerator(generator.toJson)
   }
}

