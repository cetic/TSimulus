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

package be.cetic.rtsgen.test.generators.binary

import be.cetic.rtsgen.timeseries.binary.{FalseTimeSeries, NotTimeSeries, TrueTimeSeries}
import be.cetic.rtsgen.timeseries.missing.UndefinedTimeSeries
import org.joda.time.LocalDateTime
import com.github.nscala_time.time.Imports._
import org.scalatest.{FlatSpec, Inspectors, Matchers}
import spray.json._
import be.cetic.rtsgen.generators.binary.NotGenerator
import be.cetic.rtsgen.test.RTSTest

class NotGeneratorTest extends FlatSpec with Matchers with Inspectors with RTSTest
{
   val notSource =
      """
        |{
        |   "name": "not-generator",
        |   "type": "not",
        |   "generator": "binary-generator"
        |}
      """.stripMargin

   "A NOT generator" should "be correctly read from a json document" in {
      val generator = NotGenerator(notSource.parseJson)

      generator.name shouldBe Some("not-generator")
      generator.generator shouldBe Left("binary-generator")
   }

   it should "be correctly exported to a json document" in {
      val generator = new NotGenerator(
         Some("not-generator"),
         Left("binary-generator")
      )
      generator shouldBe NotGenerator(generator.toJson)
   }
}
