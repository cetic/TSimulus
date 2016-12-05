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

package be.cetic.rtsgen.test.generators.primary

import be.cetic.rtsgen.config.{ARMAModel, GeneratorFormat}
import be.cetic.rtsgen.generators.primary.{ARMAGenerator, GaussianNoiseGenerator}
import org.joda.time.{Duration, LocalDateTime}
import org.scalatest.{FlatSpec, Matchers}
import spray.json._

class GaussianNoiseGeneratorTest extends FlatSpec with Matchers
{
   val source = """
                  |{
                  |  "name": "generator",
                  |  "type": "gaussian",
                  |  "seed": 42,
                  |  "std": 0.5
                  |}
                """.stripMargin

   "An Gaussian Noise generator" should "be correctly read from a json document" in {
      val generator = GaussianNoiseGenerator(source.parseJson)

      generator.name shouldBe Some("generator")
      generator.`type` shouldBe "gaussian"
      generator.seed shouldBe 42
      generator.std should equal (0.5F +- 0.0001F)
   }

   it should "be correctly extracted from the global extractor" in {
      noException should be thrownBy GeneratorFormat.read(source.parseJson)
   }

   it should "be correctly exported to a json document" in {
      val generator = new GaussianNoiseGenerator(
         Some("generator"),
         42,
         0.5F
      )
      generator shouldBe GaussianNoiseGenerator(generator.toJson)
   }

}
