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

import org.scalatest.{FlatSpec, Inspectors, Matchers}
import spray.json._
import be.cetic.rtsgen.config.GeneratorLeafFormat._
import be.cetic.rtsgen.generators.AndGenerator
import be.cetic.rtsgen.timeseries.binary.{AndTimeSeries, FalseTimeSeries, TrueTimeSeries}
import be.cetic.rtsgen.timeseries.missing.UndefinedTimeSeries
import org.joda.time.LocalDateTime
import com.github.nscala_time.time.Imports._


class AndGeneratorTest extends FlatSpec with Matchers with Inspectors
{
   val t = new TrueTimeSeries()
   val f = new FalseTimeSeries()
   val u = new UndefinedTimeSeries()

   val dates = Seq(
      LocalDateTime.now(),
      LocalDateTime.now() + 5.seconds,
      LocalDateTime.now() + 10.seconds
   ).toStream

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
      val document = andSource.parseJson

      val generator = document.convertTo[AndGenerator]

      generator.name shouldBe Some("and-generator")
      generator.a shouldBe Left("daily-generator")
      generator.b shouldBe Left("monthly-generator")
   }

   it should "be correctly exported to a json document" in {
      val generator = new AndGenerator(
         Some("and-generator"),
         Left("daily-generator"),
         Left("monthly-generator")
      )
      generator shouldBe generator.toJson.convertTo[AndGenerator]
   }

   "True AND True" should "be True" in {
      forAll (AndTimeSeries(t, t).compute(dates)) { result => result._2 shouldBe Some(true)}
   }

   "True AND False" should "be False" in {
      forAll (AndTimeSeries(t, f).compute(dates)) { result => result._2 shouldBe Some(false)}
   }

   "False AND True" should "be False" in {
      forAll (AndTimeSeries(f, t).compute(dates)) { result => result._2 shouldBe Some(false)}
   }

   "False AND False" should "be False" in {
      forAll (AndTimeSeries(f, f).compute(dates)) { result => result._2 shouldBe Some(false)}
   }

   "True AND Undefined" should "be Undefined" in {
      forAll (AndTimeSeries(t, u).compute(dates)) { result => result._2 shouldBe None}
   }

   "Undefined AND True" should "be Undefined" in {
      forAll (AndTimeSeries(u, t).compute(dates)) { result => result._2 shouldBe None}
   }

   "Undefined AND Undefined" should "be Undefined" in {
      forAll (AndTimeSeries(u, u).compute(dates)) { result => result._2 shouldBe None}
   }

   "False AND Undefined" should "be Undefined" in {
      forAll (AndTimeSeries(f, u).compute(dates)) { result => result._2 shouldBe None}
   }

   "Undefined AND False" should "be Undefined" in {
      forAll (AndTimeSeries(u, f).compute(dates)) { result => result._2 shouldBe None}
   }
}
