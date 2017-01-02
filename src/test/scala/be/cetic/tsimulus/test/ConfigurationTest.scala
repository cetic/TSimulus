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

package be.cetic.tsimulus.test

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

import be.cetic.tsimulus.config._
import be.cetic.tsimulus.generators.primary.{ARMAGenerator, DailyGenerator}
import be.cetic.tsimulus.generators.composite.AggregateGenerator
import be.cetic.tsimulus.generators.missing.PartialGenerator
import org.joda.time.{Duration, LocalDateTime, LocalTime}
import org.scalatest.{FlatSpec, Inspectors, Matchers}
import spray.json._

class ConfigurationTest extends FlatSpec with Matchers with Inspectors
{
   val configurationSource =
      """
        |{
        |   "generators": [
        |      {
        |         "name": "daily-generator",
        |         "type": "daily",
        |         "points": {"10:00:00.000": 4, "17:00:00.000": 32}
        |      },
        |      {
        |         "name": "noisy-daily",
        |         "type": "aggregate",
        |         "aggregator": "sum",
        |         "generators": [
        |            "daily-generator",
        |            {
        |                "type": "arma",
        |                "model": { "phi": [0.5], "std": 0.25, "c": 0, "seed": 159357},
        |                "timestep": 180000,
        |                "origin": "2016-01-01 12:34:56.789"
        |            }
        |         ]
        |      },
        |      {
        |         "name":  "partial-daily",
        |         "type": "partial",
        |         "generator": "daily-generator",
        |         "from": "2016-01-01 00:00:00.000",
        |         "to": "2017-01-01 00:00:00.000",
        |         "missing-rate": 0.01
        |      }
        |   ],
        |   "exported": [
        |      {
        |         "name": "series-A",
        |         "generator": "daily-generator",
        |         "frequency": 60000
        |      },
        |      {
        |         "name": "series-B",
        |         "generator": "noisy-daily",
        |         "frequency": 30000
        |      }
        |   ],
        |   "from": "2016-01-01 00:00:00.000",
        |   "to": "2016-10-01 00:00:00.000"
        |}
      """.stripMargin

   val completeSource =
      """
        |{
        |   "generators" : [
        |      {
        |         "name": "daily-generator",
        |         "type": "daily",
        |         "points": {"10:00:00.000": 4, "17:00:00.000": 32}
        |      },
        |      {
        |         "name": "noisy-daily",
        |         "type": "aggregate",
        |         "aggregator" : "sum",
        |         "generators": [
        |            "daily-generator",
        |            {
        |                "type": "arma",
        |                "model": { "phi": [0.5], "std": 0.25, "c" : 0, "seed": 159357},
        |                "timestep": 180000,
        |                "origin": "2016-01-01 12:34:56.789"
        |            }
        |         ]
        |      },
        |      {
        |         "name":  "partial-daily",
        |         "type": "partial",
        |         "generator" : "daily-generator",
        |         "from": "2016-01-01 00:00:00.000",
        |         "to": "2017-01-01 00:00:00.000"
        |      }
        |   ],
        |   "exported" : [
        |      {
        |         "name": "series-A",
        |         "generator": "daily-generator",
        |         "frequency": 60000
        |      },
        |      {
        |         "name": "series-B",
        |         "generator": "noisy-daily",
        |         "frequency": 30000
        |      }
        |   ],
        |   "from" : "2016-01-01 00:00:00.000",
        |   "to" : "2016-10-01 00:00:00.000"
        |}
      """.stripMargin

   "A configuration" should "be correctly read from a json document" in {
      val document = configurationSource.parseJson

      val configuration = Configuration(document)

      configuration.generators shouldBe Some(Seq(
         new DailyGenerator(
            Some("daily-generator"),
            Map(new LocalTime(10, 0, 0) -> 4, new LocalTime(17, 0, 0) -> 32)
         ),
         new AggregateGenerator(
            Some("noisy-daily"),
            "sum",
            Seq(
               Left("daily-generator"),
               Right(
                  new ARMAGenerator(
                     None,
                     ARMAModel(Some(Seq(0.5)), None, 0.25, 0, Some(159357)),
                     new LocalDateTime(2016, 1, 1, 12, 34, 56, 789),
                     new Duration(180000)
                  )
               )
            )
         ),
         new PartialGenerator(
            Some("partial-daily"),
            Left("daily-generator"),
            Some(new LocalDateTime(2016, 1, 1, 0, 0, 0)),
            Some(new LocalDateTime(2017, 1, 1, 0, 0, 0)),
            Some(0.01)
         )
      ))


      configuration.series shouldBe Seq(
         Series("series-A", Left("daily-generator"), new Duration(60000)),
         Series("series-B", Left("noisy-daily"), new Duration(30000))
      )

      configuration.from shouldBe new LocalDateTime(2016, 1, 1, 0, 0, 0)
      configuration.to shouldBe new LocalDateTime(2016, 10, 1, 0, 0, 0)
   }

   it should "be correctly exported to a json document" in {
      val generator = new PartialGenerator(
         Some("partial-generator"),
         Left("daily-generator"),
         Some(new LocalDateTime(2016, 4, 6, 0, 0, 0)),
         Some(new LocalDateTime(2016, 4, 23, 0, 0, 0)),
         Some(0.001)
      )
      generator shouldBe PartialGenerator(generator.toJson)
   }

   "A complete configuration" should "be correctly read from a json document" in {
      val document = completeSource.parseJson

      document shouldBe Configuration(document).toJson
   }
}