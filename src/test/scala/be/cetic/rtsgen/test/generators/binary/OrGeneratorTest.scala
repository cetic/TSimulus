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

import be.cetic.rtsgen.config.OrGenerator
import be.cetic.rtsgen.timeseries.binary.{FalseTimeSeries, OrTimeSeries, TrueTimeSeries}
import be.cetic.rtsgen.timeseries.missing.UndefinedTimeSeries
import org.joda.time.LocalDateTime
import com.github.nscala_time.time.Imports._
import spray.json._
import be.cetic.rtsgen.config.GeneratorLeafFormat._
import org.scalatest.{FlatSpec, Inspectors, Matchers}


class OrGeneratorTest extends FlatSpec with Matchers with Inspectors
{
   val t = new TrueTimeSeries()
   val f = new FalseTimeSeries()
   val u = new UndefinedTimeSeries()

   val dates = Seq(
      LocalDateTime.now(),
      LocalDateTime.now() + 5.seconds,
      LocalDateTime.now() + 10.seconds
   ).toStream

   val orSource =
      """
        |{
        |   "name": "or-generator",
        |   "type": "or",
        |   "a": "daily-generator",
        |   "b": "monthly-generator"
        |}
      """.stripMargin

   "A OR generator" should "be correctly read from a json document" in {
      val document = orSource.parseJson

      val generator = document.convertTo[OrGenerator]

      generator.name shouldBe Some("or-generator")
      generator.a shouldBe Left("daily-generator")
      generator.b shouldBe Left("monthly-generator")
   }

   it should "be correctly exported to a json document" in {
      val generator = new OrGenerator(
         Some("or-generator"),
         Left("daily-generator"),
         Left("monthly-generator")
      )
      generator shouldBe generator.toJson.convertTo[OrGenerator]
   }

   "True OR True" should "be True" in {
      forAll (OrTimeSeries(t, t).compute(dates)) { result => result._2 shouldBe Some(true)}
   }

   "True OR False" should "be True" in {
      forAll (OrTimeSeries(t, f).compute(dates)) { result => result._2 shouldBe Some(true)}
   }

   "False OR True" should "be True" in {
      forAll (OrTimeSeries(f, t).compute(dates)) { result => result._2 shouldBe Some(true)}
   }

   "False OR False" should "be False" in {
      forAll (OrTimeSeries(f, f).compute(dates)) { result => result._2 shouldBe Some(false)}
   }

   "True OR Undetermined" should "be Undetermined" in {
      forAll (OrTimeSeries(t, u).compute(dates)) { result => result._2 shouldBe None}
   }

   "False OR Undetermined" should "be Undetermined" in {
      forAll (OrTimeSeries(f, u).compute(dates)) { result => result._2 shouldBe None}
   }

   "Undetermined OR True" should "be Undetermined" in {
      forAll (OrTimeSeries(u, t).compute(dates)) { result => result._2 shouldBe None}
   }

   "Undetermined OR False" should "be Undetermined" in {
      forAll (OrTimeSeries(u, f).compute(dates)) { result => result._2 shouldBe None}
   }

   "Undetermined OR Undetermined" should "be Undetermined" in {
      forAll (OrTimeSeries(u, u).compute(dates)) { result => result._2 shouldBe None}
   }
}
