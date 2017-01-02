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
import org.scalatest.{FlatSpec, Inspectors, Matchers}
import spray.json._
import be.cetic.tsimulus.generators.binary.AndGenerator
import be.cetic.tsimulus.test.RTSTest

class AndGeneratorTest extends FlatSpec with Matchers with Inspectors with RTSTest
{
   val andSource =
      """
        |{
        |   "name": "and-generator",
        |   "type": "and",
        |   "a": "daily-generator",
        |   "b": "monthly-generator"
        |}
      """.stripMargin

   "A AND generator" should "be correctly read from a json document" in {
      val generator = AndGenerator(andSource.parseJson)

      generator.name shouldBe Some("and-generator")
      generator.a shouldBe Left("daily-generator")
      generator.b shouldBe Left("monthly-generator")
   }

   it should "be correctly extracted from the global extractor" in {
      noException should be thrownBy GeneratorFormat.read(andSource.parseJson)
   }

   it should "be correctly exported to a json document" in {
      val generator = new AndGenerator(
         Some("and-generator"),
         Left("daily-generator"),
         Left("monthly-generator")
      )
      generator shouldBe AndGenerator(generator.toJson)
   }

   it should "have a correct textual representation" in {
      val generator = new AndGenerator(
         Some("and-generator"),
         Left("a-generator"),
         Left("b-generator")
      )

      generator.toString shouldBe """And(Some(and-generator), Left(a-generator), Left(b-generator))"""
   }
}
