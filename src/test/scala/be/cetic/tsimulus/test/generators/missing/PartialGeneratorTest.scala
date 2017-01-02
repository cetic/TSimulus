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

package be.cetic.tsimulus.test.generators.missing

import be.cetic.tsimulus.config.GeneratorFormat
import be.cetic.tsimulus.generators.missing.PartialGenerator
import org.joda.time.LocalDateTime
import org.scalatest.{FlatSpec, Inspectors, Matchers}
import spray.json._


class PartialGeneratorTest extends FlatSpec with Matchers with Inspectors
{
   val source =
      """
        |{
        |  "name": "partial-generator",
        |  "type": "partial",
        |  "generator": "daily-generator",
        |  "from": "2016-01-01 00:00:00.000",
        |  "to": "2016-04-23 01:23:45.678",
        |  "missing-rate": 0.001
        |}
      """.stripMargin

   "A Partial generator" should "be correctly read from a json document" in {
      val generator = PartialGenerator(source.parseJson)

      generator.name shouldBe Some("partial-generator")
      generator.generator shouldBe Left("daily-generator")
      generator.from shouldBe Some(new LocalDateTime(2016, 1, 1, 0, 0, 0))
      generator.to shouldBe Some(new LocalDateTime(2016, 4, 23, 1, 23, 45, 678))
      generator.missingRate shouldBe Some(0.001)
   }

   it should "be correctly extracted from the global extractor" in {
      noException should be thrownBy GeneratorFormat.read(source.parseJson)
   }

   it should "be correctly exported to a json document" in {
      val generator = new PartialGenerator(
         Some("limited-generator"),
         Left("daily-generator"),
         Some(new LocalDateTime(2016, 1, 1, 0, 0, 0)),
         Some(new LocalDateTime(2016, 4, 23, 1, 23, 45, 678)),
         Some(0.001)
      )
      generator shouldBe PartialGenerator(generator.toJson)
   }
}
