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

import org.joda.time.{Duration, LocalDateTime}
import org.scalatest.{FlatSpec, Inspectors, Matchers}
import spray.json._
import be.cetic.rtsgen.config.GeneratorLeafFormat._
import be.cetic.rtsgen.generators.composite.TransitionGenerator

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
      val document = transitionSource.parseJson

      val generator = document.convertTo[TransitionGenerator]

      generator.name shouldBe Some("transition-generator")
      generator.first shouldBe Left("first-generator")
      generator.second shouldBe Left("second-generator")
      generator.time shouldBe new LocalDateTime(2016, 6, 7, 3, 45, 0)
      generator.interval shouldBe Some(new Duration(300000))
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
      generator shouldBe generator.toJson.convertTo[TransitionGenerator]
   }

   it should "correctly import its transition related parameters" in {
      val document = otherTransitionSource.parseJson

      val generator = document.convertTo[TransitionGenerator]

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
}
