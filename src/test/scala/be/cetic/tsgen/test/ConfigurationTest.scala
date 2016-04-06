package be.cetic.tsgen.test

import be.cetic.tsgen.config.{ARMAGenerator, ARMAModel, DailyGenerator}
import org.joda.time.{Duration, LocalTime}
import org.scalatest.{FlatSpec, Matchers}
import spray.json._
import be.cetic.tsgen.config.GeneratorLeafFormat._


class ConfigurationTest extends FlatSpec with Matchers {
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

   val dailySource =
      """
        |{
        |    "name": "daily-generator",
        |    "type": "daily",
        |    "points": {"11:00:00.000" : 6, "17:00:00.000" : 8, "07:00:00.000" : 2}
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
      val generator = ARMAGenerator(Some("g3"), "arma", new ARMAModel(Some(Seq(1,2,3)), Some(Seq(4,3,2,1)), 0.5, 4.2, Some(1809)), new Duration(180000))
      generator shouldBe generator.toJson.convertTo[ARMAGenerator]
   }

   "A daily generator" should "be correctly read from a json document" in {
      val document = dailySource.parseJson

      val generator = document.convertTo[DailyGenerator]

      generator.name shouldBe Some("daily-generator")
      generator.`type` shouldBe "daily"
      generator.points shouldBe Map(
         new LocalTime(11,0,0) -> 6,
         new LocalTime(17,0,0) -> 8,
         new LocalTime(7,0,0) -> 2)
   }

   it should "be correctly exported to a json document" in {
      val generator = DailyGenerator(Some("daily-generator"), "daily", Map(
         new LocalTime(11,0,0) -> 6,
         new LocalTime(17,0,0) -> 8,
         new LocalTime(7,0,0) -> 2))

      generator shouldBe generator.toJson.convertTo[DailyGenerator]
   }


}