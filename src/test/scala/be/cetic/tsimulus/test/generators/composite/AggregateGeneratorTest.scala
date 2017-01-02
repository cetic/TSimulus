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
import org.scalatest.{FlatSpec, Matchers}
import spray.json._
import be.cetic.tsimulus.generators.composite.AggregateGenerator

class AggregateGeneratorTest extends FlatSpec with Matchers
{
   val aggregateSource =
      """
        |{
        |   "name": "aggregate-generator",
        |   "type": "aggregate",
        |   "aggregator": "sum",
        |   "generators": ["daily-generator", "monthly-generator"]
        |}
      """.stripMargin

   "An aggregate generator" should "be correctly read from a json document" in {
      val generator = AggregateGenerator(aggregateSource.parseJson)

      generator.name shouldBe Some("aggregate-generator")
      generator.aggregator shouldBe "sum"
      generator.generators shouldBe Seq(Left("daily-generator"), Left("monthly-generator"))
   }

   it should "be correctly extracted from the global extractor" in {
      noException should be thrownBy GeneratorFormat.read(aggregateSource.parseJson)
   }

   it should "be correctly exported to a json document" in {
      val generator = new AggregateGenerator(
         Some("aggregate-generator"),
         "sum",
         Seq(Left("daily-generator"), Left("monthly-generator"))
      )
      generator shouldBe AggregateGenerator(generator.toJson)
   }

   it should "have a correct textual representation" in {
      val generator = new AggregateGenerator(
         Some("aggregate-generator"),
         "sum",
         Seq(Left("a-generator"), Left("b-generator"))
      )

      generator.toString shouldBe """Aggregate(Some(aggregate-generator), sum, [Left(a-generator), Left(b-generator)])"""
   }
}
