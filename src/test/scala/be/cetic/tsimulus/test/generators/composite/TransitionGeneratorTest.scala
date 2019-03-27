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
import org.joda.time.{Duration, LocalDateTime}
import org.scalatest.{FlatSpec, Inspectors, Matchers}
import spray.json._
import be.cetic.tsimulus.generators.composite.TransitionGenerator

class TransitionGeneratorTest extends FlatSpec with Matchers with Inspectors
{
   val transitionSource =
      """
        |{
        |   "name" : "transition-generator",
        |   "first" : "first-generator",
        |   "second" : "second-generator",
        |   "type": "transition",
        |   "time" : "2016-06-07 03:45:00.000",
        |   "duration": 300000,
        |   "transition": "linear"
        |}
      """.stripMargin

   val otherTransitionSource =
      """
        |{
        |   "name" : "transition-generator",
        |   "first" : "first-generator",
        |   "second" : "second-generator",
        |   "type": "transition",
        |   "time" : "2016-06-07 03:45:00.000",
        |   "duration": 300000,
        |   "transition": "sigmoid"
        |}
      """.stripMargin

   "A transition generator" should "be correctly read from a json document" in {
      val generator = TransitionGenerator(transitionSource.parseJson)

      generator.name shouldBe Some("transition-generator")
      generator.first shouldBe Left("first-generator")
      generator.second shouldBe Left("second-generator")
      generator.time shouldBe new LocalDateTime(2016, 6, 7, 3, 45, 0)
      generator.interval shouldBe Some(new Duration(300000))
   }

   it should "be extracted from the global extractor without any error" in {
      noException should be thrownBy GeneratorFormat.read(transitionSource.parseJson)
   }

   it should "be correctly extracted from the global extractor" in {
      GeneratorFormat.read(transitionSource.parseJson) shouldBe TransitionGenerator(transitionSource.parseJson)
   }

   it should "be correctly exported to a json document" in {
      val generator = new TransitionGenerator(
         Some("transition-generator"),
         Left("first-generator"),
         Left("second-generator"),
         new LocalDateTime(2016, 6, 7, 3, 45, 0),
         None,
         None
      )
      generator shouldBe TransitionGenerator(generator.toJson)
   }

   it should "correctly import its transition related parameters" in {
      val generator = TransitionGenerator(otherTransitionSource.parseJson)

      generator.f shouldBe Some("sigmoid")
      generator.interval shouldBe Some(new Duration(300000))
   }

   it should "correctly export its transition related parameters" in {
      val generator = new TransitionGenerator(
         Some("transition-generator"),
         Left("first-generator"),
         Left("second-generator"),
         new LocalDateTime(2016, 6, 7, 3, 45, 0),
         Some(new Duration(42)),
         Some("sigmoid")
      )

      val fields = generator.toJson.asJsObject.fields

      fields.get("transition") shouldBe 'defined
      fields("transition") match { case JsString(x) => x shouldBe "sigmoid" }
   }

   it should "have a correct textual representation" in {
      val generator = new TransitionGenerator(
         Some("transition-generator"),
         Left("a-generator"),
         Left("b-generator"),
         new LocalDateTime(2016, 6, 7, 3, 45, 0),
         Some(new Duration(42)),
         Some("sigmoid")
      )

      generator.toString shouldBe """Transition(Some(transition-generator), Left(a-generator), Left(b-generator), 2016-06-07T03:45:00.000, Some(PT0.042S), Some(sigmoid))"""
   }
}
