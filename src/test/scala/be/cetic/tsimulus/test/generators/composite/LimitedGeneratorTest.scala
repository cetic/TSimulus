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

package be.cetic.tsimulus.test.generators.composite

import be.cetic.tsimulus.config.GeneratorFormat
import org.joda.time.LocalDateTime
import org.scalatest.{FlatSpec, Matchers}
import spray.json._
import be.cetic.tsimulus.generators.missing.LimitedGenerator

class LimitedGeneratorTest extends FlatSpec with Matchers
{
   val source =
      """
        |{
        |   "name" : "limited-generator",
        |   "type": "limited",
        |   "generator": "daily-generator",
        |   "from": "2016-04-06 00:00:00.000",
        |   "to": "2016-04-23 00:00:00.000"
        |}
      """.stripMargin

   "A limited generator" should "be correctly read from a json document" in {
      val generator = LimitedGenerator(source.parseJson)

      generator.name shouldBe Some("limited-generator")
      generator.generator shouldBe Left("daily-generator")
      generator.from shouldBe Some(new LocalDateTime(2016, 4, 6, 0, 0, 0))
      generator.to shouldBe Some(new LocalDateTime(2016, 4, 23, 0, 0, 0))
   }

   it should "be extracted from the global extractor without any error" in {
      noException should be thrownBy GeneratorFormat.read(source.parseJson)
   }

   it should "be correctly extracted from the global extractor" in {
      GeneratorFormat.read(source.parseJson) shouldBe LimitedGenerator(source.parseJson)
   }

   it should "be correctly exported to a json document" in {
      val generator = new LimitedGenerator(
         Some("limited-generator"),
         Left("daily-generator"),
         Some(new LocalDateTime(2016, 4, 6, 0, 0, 0)),
         Some(new LocalDateTime(2016, 4, 23, 0, 0, 0))
      )
      generator shouldBe LimitedGenerator(generator.toJson)
   }

   it should "have a correct textual representation" in {
      val generator = new LimitedGenerator(
         Some("limited-generator"),
         Left("daily-generator"),
         Some(new LocalDateTime(2016, 4, 6, 0, 0, 0)),
         Some(new LocalDateTime(2016, 4, 23, 0, 0, 0))
      )

      generator.toString shouldBe """Limited(Some(limited-generator), Left(daily-generator), Some(2016-04-06T00:00:00.000), Some(2016-04-23T00:00:00.000))"""
   }
}
