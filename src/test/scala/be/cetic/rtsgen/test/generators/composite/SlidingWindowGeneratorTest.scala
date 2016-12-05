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
import be.cetic.rtsgen.generators.composite.SlidingWindowGenerator
import org.joda.time.{Duration, LocalDateTime}

class SlidingWindowGeneratorTest extends FlatSpec with Matchers
{
   val source =
      """
        |{
        |  "name": "window-generator",
        |  "type": "window",
        |  "aggregator": "sum",
        |  "n": 5,
        |  "window-length" : 5000,
        |  "generator": "daily-generator"
        |}
      """.stripMargin

   "A Sliding Window generator" should "be correctly read from a json document" in {
      val generator = SlidingWindowGenerator(source.parseJson)

      generator.name shouldBe Some("window-generator")
      generator.aggregator shouldBe "sum"
      generator.duration shouldBe new Duration(5000)
      generator.generator shouldBe Left("daily-generator")
   }

   it should "be correctly extracted from the global extractor" in {
      noException should be thrownBy GeneratorFormat.read(source.parseJson)
   }

   it should "be correctly exported to a json document" in {
      val generator = new SlidingWindowGenerator(
         Some("window-generator"),
         "sum",
         Left("daily-generator"),
         5,
         new Duration(5000)
      )
      generator shouldBe SlidingWindowGenerator(generator.toJson)
   }

   it should "have a correct textual representation" in {
      val generator = new SlidingWindowGenerator(
         Some("window-generator"),
         "sum",
         Left("daily-generator"),
         5,
         new Duration(5000)
      )

      generator.toString shouldBe """SlidingWindow(Some(window-generator), sum, Left(daily-generator), PT5S)"""
   }
}
