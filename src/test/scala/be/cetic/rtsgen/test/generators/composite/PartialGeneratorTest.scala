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
import be.cetic.rtsgen.generators.missing.PartialGenerator
import org.joda.time.LocalDateTime

class PartialGeneratorTest extends FlatSpec with Matchers
{
   val source =
      """
        |{
        |   "name" : "partial-generator",
        |   "type" : "partial",
        |   "generator": "daily-generator",
        |   "from": "2016-04-06 00:00:00.000",
        |   "to": "2016-04-23 00:00:00.000",
        |   "missing-rate" : 0.001
        |}
      """.stripMargin

   "A partial generator" should "be correctly read from a json document" in {
      val generator = PartialGenerator(source.parseJson)

      generator.name shouldBe Some("partial-generator")
      generator.generator shouldBe Left("daily-generator")
      generator.from shouldBe Some(new LocalDateTime(2016, 4, 6, 0, 0, 0))
      generator.to shouldBe Some(new LocalDateTime(2016, 4, 23, 0, 0, 0))
      generator.missingRate shouldBe Some(0.001)
   }

   it should "be correctly extracted from the global extractor" in {
      noException should be thrownBy GeneratorFormat.read(source.parseJson)
   }

   it should "be correctly exported to a json document" in {
      val generator = new PartialGenerator(
         Some("partial-generator"),
         Left("daily-generator"),
         Some(new LocalDateTime(2016, 4, 6, 0, 0, 0)),
         Some(new LocalDateTime(2016, 4, 23, 0, 0, 0)),
         Some(0.2)
      )
      generator shouldBe PartialGenerator(generator.toJson)
   }

   it should "have a correct textual representation" in {
      val generator = new PartialGenerator(
         Some("partial-generator"),
         Left("daily-generator"),
         Some(new LocalDateTime(2016, 4, 6, 0, 0, 0)),
         Some(new LocalDateTime(2016, 4, 23, 0, 0, 0)),
         Some(0.2)
      )

      generator.toString shouldBe """Partial(Some(partial-generator), Left(daily-generator), Some(2016-04-06T00:00:00.000), Some(2016-04-23T00:00:00.000), Some(0.2))"""
   }
}
