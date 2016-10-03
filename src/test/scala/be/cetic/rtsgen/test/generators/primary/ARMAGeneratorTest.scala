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

import be.cetic.rtsgen.config.{ARMAGenerator, ARMAModel}
import org.joda.time.Duration
import org.scalatest.{FlatSpec, Matchers}

import spray.json._
import be.cetic.rtsgen.config.GeneratorLeafFormat._

class ARMAGeneratorTest extends FlatSpec with Matchers
{
   val armaSource = """
                      |{
                      |  "name": "g3",
                      |  "type": "arma",
                      |  "model": {
                      |      "phi": [1,2,3],
                      |      "theta": [4,3,2,1],
                      |      "std": 0.5,
                      |      "c": 4.2,
                      |      "seed": 1809
                      |   },
                      |   "timestep": 180000
                      |}
                    """.stripMargin

   "An ARMA generator" should "be correctly read from a json document" in {
      val document = armaSource.parseJson

      val generator = document.convertTo[ARMAGenerator]


      generator.name shouldBe Some("g3")
      generator.`type` shouldBe "arma"
      generator.timestep shouldBe new Duration(180000)
   }

   it should "be correctly exported to a json document" in {
      val generator = new ARMAGenerator(
         Some("g3"),
         ARMAModel(
            Some(Seq(1, 2, 3)),
            Some(Seq(4, 3, 2, 1)),
            0.5,
            4.2,
            Some(1809)
         ),
         new Duration(180000)
      )
      generator shouldBe generator.toJson.convertTo[ARMAGenerator]
   }
}
