package be.cetic.tsgen.test

import be.cetic.tsgen.config._
import org.joda.time.{Duration, LocalDateTime, LocalTime}
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

   val monthlySource =
      """
        |{
        |   "name": "monthly-generator",
        |   "type": "monthly",
        |   "points":  {"january": -6.3, "february": -6.9, "june" : -2.7}
        |}
      """.stripMargin

   val weeklySource =
      """
        |{
        |   "name": "weekly-generator",
        |   "type": "weekly",
        |   "points": {"monday": 8.7, "friday": -3.6, "sunday" : 10.9}
        |}
      """.stripMargin

   val yearlySource =
      """
        |{
        |   "name": "yearly-generator",
        |   "type": "yearly",
        |   "points": {"2015": 42.12, "2016": 13.37, "2017": 6.022}
        |}
        |
      """.stripMargin

   val constantSource =
      """
        |{
        |   "name": "constant-generator",
        |   "type": "constant",
        |   "value":  17.5
        |}
      """.stripMargin

   val functionSource =
      """
        |{
        |   "name": "function-generator",
        |   "type": "function",
        |   "generator": { "type" : "constant", "value" : 42 },
        |   "slope": 1.4,
        |   "intercept" : 9.2
        |}
      """.stripMargin

   val aggregateSource =
      """
        |{
        |   "name": "aggregate-generator",
        |   "type": "aggregate",
        |   "aggregator": "sum",
        |   "generators": ["daily-generator", "monthly-generator"]
        |}
      """.stripMargin

   val correlatedSource =
      """
        |{
        |   "name": "corr-generator",
        |   "type": "correlated",
        |   "generator": "daily-generator",
        |   "coef": 0.8
        |}
      """.stripMargin

   val logisticSource =
      """
        |{
        |   "name": "logistic-generator",
        |   "type": "logistic",
        |   "generator": "daily-generator",
        |   "location": 6,
        |   "scale": 2.4,
        |   "seed": 1809
        |}
      """.stripMargin

   val transitionSource =
      """
        |{
        |   "name" : "transition-generator",
        |   "first" : "first-generator",
        |   "second" : "second-generator",
        |   "type": "transition",
        |   "time" : "2016-06-07 03:45:00.000"
        |}
      """.stripMargin

   val limitedSource =
      """
        |{
        |   "name" : "limited-generator",
        |   "type": "limited",
        |   "generator": "daily-generator",
        |   "from": "2016-04-06 00:00:00.000",
        |   "to": "2016-04-23 00:00:00.000"
        |}
      """.stripMargin

   val partialSource =
      """
        |{
        |   "name" : "partial-generator",
        |   "type" : "partial",
        |   "generator": "daily-generator",
        |   "from": "2016-04-06 00:00:00.000",
        |   "to": "2016-04-23 00:00:00.000",
        |   "missing-rate" : 0.001
        |}
      """.stripMargin

   val seriesSource =
      """
        |{
        |   "name": "myName",
        |   "generator": "daily-generator",
        |   "frequency": 60000
        |}
      """.stripMargin

   val timeShiftedSource =
      """
        |{
        |   "name": "time-shifted-generator",
        |   "type": "time-shift",
        |   "generator": "daily-generator",
        |   "shift": -8000
        |}
      """.stripMargin

   val thresholdSource =
      """
        |{
        |   "name": "threshold-generator",
        |   "type": "threshold",
        |   "generator": "daily-generator",
        |   "threshold": 42,
        |   "included": true
        |}
      """.stripMargin

   val andSource =
      """
        |{
        |   "name": "and-generator",
        |   "type": "and",
        |   "a": "daily-generator",
        |   "b": "monthly-generator"
        |}
      """.stripMargin

   val orSource =
      """
        |{
        |   "name": "or-generator",
        |   "type": "or",
        |   "a": "daily-generator",
        |   "b": "monthly-generator"
        |}
      """.stripMargin

   val notSource =
      """
        |{
        |   "name": "not-generator",
        |   "type": "not",
        |   "generator": "binary-generator"
        |}
      """.stripMargin

   val xorSource =
      """
        |{
        |   "name": "xor-generator",
        |   "type": "xor",
        |   "a": "daily-generator",
        |   "b": "monthly-generator"
        |}
      """.stripMargin

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
        |                "timestep": 180000
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
        |   "series": [
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
        |                "timestep": 180000
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
        |   "series" : [
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
         new ARMAModel(
            Some(Seq(1,2,3)),
            Some(Seq(4,3,2,1)),
            0.5,
            4.2,
            Some(1809)
         ),
         new Duration(180000)
      )
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
      val generator = new DailyGenerator(Some("daily-generator"), Map(
         new LocalTime(11,0,0) -> 6,
         new LocalTime(17,0,0) -> 8,
         new LocalTime(7,0,0) -> 2))

      generator shouldBe generator.toJson.convertTo[DailyGenerator]
   }

   "A weekly generator" should "be correctly read from a json document" in {
      val document = weeklySource.parseJson

      val generator = document.convertTo[WeeklyGenerator]

      generator.name shouldBe Some("weekly-generator")
      generator.`type` shouldBe "weekly"
      generator.points shouldBe Map(
         "monday" -> 8.7,
         "friday" -> -3.6,
         "sunday" -> 10.9)
   }

   it should "be correctly exported to a json document" in {
      val generator = new WeeklyGenerator(Some("weekly-generator"), Map(
         "monday" -> 8.7,
         "friday" -> -3.6,
         "sunday" -> 10.9))

      generator shouldBe generator.toJson.convertTo[WeeklyGenerator]
   }

   "A monthly generator" should "be correctly read from a json document" in {
      val document = monthlySource.parseJson

      val generator = document.convertTo[MonthlyGenerator]

      generator.name shouldBe Some("monthly-generator")
      generator.`type` shouldBe "monthly"
      generator.points shouldBe Map(
         "january" -> -6.3,
         "february" -> -6.9,
         "june" -> -2.7
      )
   }

   it should "be correctly exported to a json document" in {
      val generator = new MonthlyGenerator(Some("monthly-generator"), Map(
         "january" -> -6.3,
         "february" -> -6.9,
         "june" -> -2.7
      ))

      generator shouldBe generator.toJson.convertTo[MonthlyGenerator]
   }

   "A yearly generator" should "be correctly read from a json document" in {
      val document = yearlySource.parseJson

      val generator = document.convertTo[YearlyGenerator]

      generator.name shouldBe Some("yearly-generator")
      generator.`type` shouldBe "yearly"
      generator.points shouldBe Map(2015 -> 42.12, 2016 -> 13.37, 2017 -> 6.022)
   }

   it should "be correctly exported to a json document" in {
      val generator = new YearlyGenerator(Some("yearly-generator"), Map(
         2015 -> 42.12,
         2016 -> 13.37,
         2017 -> 6.022)
      )

      generator shouldBe generator.toJson.convertTo[YearlyGenerator]
   }

   "A constant generator" should "be correctly read from a json document" in {
      val document = constantSource.parseJson

      val generator = document.convertTo[ConstantGenerator]

      generator.name shouldBe Some("constant-generator")
      generator.`type` shouldBe "constant"
      generator.value shouldBe 17.5
   }

   it should "be correctly exported to a json document" in {
      val generator = new ConstantGenerator(Some("constant-generator"), 17.5)
      generator shouldBe generator.toJson.convertTo[ConstantGenerator]
   }

   "A function generator" should "be correctly read from a json document" in {
      val document = functionSource.parseJson

      val generator = document.convertTo[FunctionGenerator]

      generator.name shouldBe Some("function-generator")
      generator.`type` shouldBe "function"
      generator.slope shouldBe 1.4
      generator.intercept shouldBe 9.2
      generator.generator shouldBe Right(new ConstantGenerator(None, 42))
   }

   it should "be correctly exported to a json document" in {
      val generator = new FunctionGenerator(
         Some("function-generator"),
         Right(new ConstantGenerator(None, 42)),
         1.4,
         9.2
      )
      val a = generator.toJson

      val b = generator shouldBe a.convertTo[FunctionGenerator]
   }

   "An aggregate generator" should "be correctly read from a json document" in {
      val document = aggregateSource.parseJson

      val generator = document.convertTo[AggregateGenerator]

      generator.name shouldBe Some("aggregate-generator")
      generator.aggregator shouldBe "sum"
      generator.generators shouldBe Seq(Left("daily-generator"), Left("monthly-generator"))

   }

   it should "be correctly exported to a json document" in {
      val generator = new AggregateGenerator(
         Some("aggregate-generator"),
         "sum",
         Seq(Left("daily-generator"), Left("monthly-generator"))
      )
      generator shouldBe generator.toJson.convertTo[AggregateGenerator]
   }

   "A correlated generator" should "be correctly read from a json document" in {
      val document = correlatedSource.parseJson

      val generator = document.convertTo[CorrelatedGenerator]

      generator.name shouldBe Some("corr-generator")
      generator.generator shouldBe Left("daily-generator")
      generator.coef shouldBe 0.8
   }

   it should "be correctly exported to a json document" in {
      val generator = new CorrelatedGenerator(
         Some("corr-generator"),
         Left("daily-generator"),
         0.8
      )
      generator shouldBe generator.toJson.convertTo[CorrelatedGenerator]
   }

   "A logistic generator" should "be correctly read from a json document" in {
      val document = logisticSource.parseJson

      val generator = document.convertTo[LogisticGenerator]

      generator.name shouldBe Some("logistic-generator")
      generator.generator shouldBe Left("daily-generator")
      generator.location shouldBe 6
      generator.scale shouldBe 2.4
      generator.seed shouldBe Some(1809)
   }

   it should "be correctly exported to a json document" in {
      val generator = new LogisticGenerator(
         Some("logistic-generator"),
         Left("daily-generator"),
         6,
         2.4,
         Some(1809)
      )
      generator shouldBe generator.toJson.convertTo[LogisticGenerator]
   }

   "A transition generator" should "be correctly read from a json document" in {
      val document = transitionSource.parseJson

      val generator = document.convertTo[TransitionGenerator]

      generator.name shouldBe Some("transition-generator")
      generator.first shouldBe Left("first-generator")
      generator.second shouldBe Left("second-generator")
      generator.time shouldBe new LocalDateTime(2016, 6, 7, 3, 45, 0)
      generator.interval shouldBe None
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


   "A limited generator" should "be correctly read from a json document" in {
      val document = limitedSource.parseJson

      val generator = document.convertTo[LimitedGenerator]

      generator.name shouldBe Some("limited-generator")
      generator.generator shouldBe Left("daily-generator")
      generator.from shouldBe Some(new LocalDateTime(2016, 4, 6, 0, 0, 0))
      generator.to shouldBe Some(new LocalDateTime(2016, 4, 23, 0, 0, 0))
   }

   it should "be correctly exported to a json document" in {
      val generator = new LimitedGenerator(
         Some("limited-generator"),
         Left("daily-generator"),
         Some(new LocalDateTime(2016, 4, 6, 0, 0, 0)),
         Some(new LocalDateTime(2016, 4, 23, 0, 0, 0))
      )
      generator shouldBe generator.toJson.convertTo[LimitedGenerator]
   }

   "A partial generator" should "be correctly read from a json document" in {
      val document = partialSource.parseJson

      val generator = document.convertTo[PartialGenerator]

      generator.name shouldBe Some("partial-generator")
      generator.generator shouldBe Left("daily-generator")
      generator.from shouldBe Some(new LocalDateTime(2016, 4, 6, 0, 0, 0))
      generator.to shouldBe Some(new LocalDateTime(2016, 4, 23, 0, 0, 0))
      generator.missingRate shouldBe Some(0.001)
   }

   it should "be correctly exported to a json document" in {
      val generator = new PartialGenerator(
         Some("partial-generator"),
         Left("daily-generator"),
         Some(new LocalDateTime(2016, 4, 6, 0, 0, 0)),
         Some(new LocalDateTime(2016, 4, 23, 0, 0, 0)),
         Some(0.2)
      )
      generator shouldBe generator.toJson.convertTo[PartialGenerator]
   }

   "A threshold generator" should "be correctly read from a json document" in {
      val document = thresholdSource.parseJson

      val generator = document.convertTo[ThresholdGenerator]

      generator.name shouldBe Some("threshold-generator")
      generator.generator shouldBe Left("daily-generator")
      generator.threshold shouldBe 42
      generator.included shouldBe Some(true)
   }

   it should "be correctly exported to a json document" in {
      val generator = new ThresholdGenerator(
         Some("threshold-generator"),
         Left("daily-generator"),
         42,
         Some(false)
      )
      generator shouldBe generator.toJson.convertTo[ThresholdGenerator]
   }

   "A time shifted generator" should "be correctly read from a json document" in {
      val document = timeShiftedSource.parseJson

      val generator = document.convertTo[TimeShiftGenerator]

      generator.name shouldBe Some("time-shifted-generator")
      generator.generator shouldBe Left("daily-generator")
      generator.shift shouldBe new Duration(-8000)
   }

   it should "be correctly exported to a json document" in {
      val generator = new TimeShiftGenerator(
         Some("time-shifted-generator"),
         Left("daily-generator"),
         new Duration(-8000)
      )
      generator shouldBe generator.toJson.convertTo[TimeShiftGenerator]
   }

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

   "A NOT generator" should "be correctly read from a json document" in {
      val document = notSource.parseJson

      val generator = document.convertTo[NotGenerator]

      generator.name shouldBe Some("not-generator")
      generator.generator shouldBe Left("binary-generator")
   }

   it should "be correctly exported to a json document" in {
      val generator = new NotGenerator(
         Some("not-generator"),
         Left("binary-generator")
      )
      generator shouldBe generator.toJson.convertTo[NotGenerator]
   }

   "A XOR generator" should "be correctly read from a json document" in {
      val document = xorSource.parseJson

      val generator = document.convertTo[XorGenerator]

      generator.name shouldBe Some("xor-generator")
      generator.a shouldBe Left("daily-generator")
      generator.b shouldBe Left("monthly-generator")
   }

   it should "be correctly exported to a json document" in {
      val generator = new XorGenerator(
         Some("xor-generator"),
         Left("daily-generator"),
         Left("monthly-generator")
      )
      generator shouldBe generator.toJson.convertTo[XorGenerator]
   }



   "A series" should "be correctly read from a json document" in {
      val document = seriesSource.parseJson

      val series = document.convertTo[Series[Any]]

      series.name shouldBe "myName"
      series.generator shouldBe Left("daily-generator")
      series.frequency shouldBe new Duration(60000)
   }

   it should "be correctly exported to a json document" in {
      val series = Series[Any](
         "myName",
         Left("daily-generator"),
         new Duration(60000)
      )
      series shouldBe SeriesFormat.read(SeriesFormat.write(series))
   }

   "A configuration" should "be correctly read from a json document" in {
      val document = configurationSource.parseJson

      val configuration = document.convertTo[Configuration]

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
      generator shouldBe generator.toJson.convertTo[PartialGenerator]
   }

   "A complete configuration" should "be correctly read from a json document" in {
      val document = completeSource.parseJson

      document shouldBe document.convertTo[Configuration].toJson
   }
}