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

package be.cetic.tsimulus.test.generators.binary

import be.cetic.tsimulus.config.GeneratorFormat
import be.cetic.tsimulus.timeseries.binary.{FalseTimeSeries, NotTimeSeries, TrueTimeSeries}
import be.cetic.tsimulus.timeseries.missing.UndefinedTimeSeries
import org.joda.time.LocalDateTime
import com.github.nscala_time.time.Imports._
import org.scalatest.{FlatSpec, Inspectors, Matchers}
import spray.json._
import be.cetic.tsimulus.generators.binary.NotGenerator
import be.cetic.tsimulus.test.RTSTest

class NotGeneratorTest extends FlatSpec with Matchers with Inspectors with RTSTest
{
   val source =
      """
        |{
        |   "name": "not-generator",
        |   "type": "not",
        |   "generator": "binary-generator"
        |}
      """.stripMargin

   "A NOT generator" should "be correctly read from a json document" in {
      val generator = NotGenerator(source.parseJson)

      generator.name shouldBe Some("not-generator")
      generator.generator shouldBe Left("binary-generator")
   }

   it should "be extracted from the global extractor without any error" in {
      noException should be thrownBy GeneratorFormat.read(source.parseJson)
   }

   it should "be correctly extracted from the global extractor" in {
      GeneratorFormat.read(source.parseJson) shouldBe NotGenerator(source.parseJson)
   }

   it should "be correctly exported to a json document" in {
      val generator = new NotGenerator(
         Some("not-generator"),
         Left("binary-generator")
      )
      generator shouldBe NotGenerator(generator.toJson)
   }

   it should "have a correct textual representation" in {
      val generator = new NotGenerator(
         Some("not-generator"),
         Left("a-generator")
      )

      generator.toString shouldBe """Not(Some(not-generator), Left(a-generator))"""
   }
}
