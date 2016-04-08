package be.cetic.tsgen.config

import com.github.nscala_time.time.Imports._
import org.joda.time.{LocalDateTime, LocalTime}
import spray.json._

class Generator(name: Option[String], `type`: String)

case class InvalidGenerator(name: String) extends Generator(Some(name), "ERROR")

case class Configuration(generators: Seq[Generator],
                         series: Seq[Series],
                         from: LocalDateTime,
                         to: LocalDateTime)

case class Series(generator: Either[String, Generator], frequency: Long)

case class ARMAGenerator(name: Option[String],
                         `type`: String,
                         val model: ARMAModel,
                         val timestep: Duration) extends Generator(name, `type`)

case class ARMAModel(phi: Option[Seq[Double]],
                     theta: Option[Seq[Double]],
                     std: Double,
                     c: Double,
                     seed: Option[Int])

case class DailyGenerator(name: Option[String],
                          `type`: String,
                          val points: Map[LocalTime, Double]) extends Generator(name, `type`)

case class WeeklyGenerator(name: Option[String],
                           `type`: String,
                           val points: Map[String, Double]) extends Generator(name, `type`)

case class MonthlyGenerator(name: Option[String],
                            `type`: String,
                            val points: Map[String, Double]) extends Generator(name, `type`)

case class YearlyGenerator(name: Option[String],
                           `type`: String,
                           val points: Map[Int, Double]) extends Generator(name, `type`)

case class ConstantGenerator(name: Option[String],
                             `type`: String,
                             val value: Double) extends Generator(name, `type`)

case class FunctionGenerator(name: Option[String],
                             `type`: String,
                             val generator: Either[String, Generator],
                             val coef: Double,
                             val offset: Double) extends Generator(name, `type`)

case class AggregateGenerator(name: Option[String],
                              `type`: String,
                              val aggregator: String,
                              val generators: Seq[Either[String, Generator]]) extends Generator(name, `type`)

case class CorrelatedGenerator(name: Option[String],
                               `type`: String,
                               val generator: Either[String, Generator],
                               val coef: Double) extends Generator(name, `type`)

case class LogisticGenerator(name: Option[String],
                             `type`: String,
                             val generator: Either[String, Generator],
                             val location: Double,
                             val scale: Double,
                             val seed: Option[Long]) extends Generator(name, `type`)

case class TransitionGenerator(name: Option[String],
                               `type`: String,
                               val origin: Either[String, Generator],
                               val transitions: Seq[Transition]) extends Generator(name, `type`)

case class Transition(generator: Either[String, Generator], start: LocalDateTime, delay: Option[Duration])

case class LimitedGenerator(name: Option[String],
                            `type`: String,
                            val generator: Either[String, Generator],
                            val from: Option[LocalDateTime],
                            val to: Option[LocalDateTime],
                            val missingRate: Option[Double]) extends Generator(name, `type`)

case class PartialGenerator(name: Option[String],
                            `type`: String,
                            val generator: Either[String, Generator],
                            val from: Option[LocalDateTime],
                            val to: Option[LocalDateTime]) extends Generator(name, `type`)

/*
 * http://stackoverflow.com/questions/32721636/spray-json-serializing-inheritance-case-class
 */
object GeneratorLeafFormat extends DefaultJsonProtocol
{
   val dtf = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss.SSS")
   val ttf = DateTimeFormat.forPattern("HH:mm:ss.SSS")

   implicit object LocalDateTimeJsonFormat extends RootJsonFormat[LocalDateTime] {
      def write(d: LocalDateTime) = JsString(dtf.print(d))
      def read(value: JsValue) = value match {
         case JsString(s) => dtf.parseLocalDateTime(s)
         case unrecognized => serializationError(s"Serialization problem ${unrecognized}")
      }
   }

   implicit object LocalTimeJsonFormat extends RootJsonFormat[LocalTime] {
      def write(t: LocalTime) = JsString(ttf.print(t))
      def read(value: JsValue) = value match {
         case JsString(s) => ttf.parseLocalTime(s)
         case unknown => deserializationError(s"unknown LocalTime object: ${unknown}")
      }
   }

   implicit object YearlyFormat extends RootJsonFormat[YearlyGenerator] {

      override def write(obj: YearlyGenerator): JsValue = {
         val name = obj.name.toJson
         val `type` = obj.`type`.toJson
         val points = obj.points map {case (k, v) => (k.toString, v)} toJson

         new JsObject(Map("name" -> name, "type" -> `type`, "points" -> points))
      }

      override def read(json: JsValue): YearlyGenerator = {
         val name = json.asJsObject.fields("name") match {
            case JsString(x) => Some(x)
            case _ => None
         }

         val `type` = json.asJsObject.fields("type") match {
            case JsString(x) => x
         }

         val points = json.asJsObject.fields("points") match {
            case JsObject(x) => x
         }

         val r = points map { case (k,v) => (k.toInt, v match { case JsNumber(x) => x.toDouble })}

         YearlyGenerator(name, `type`, r)
      }
   }

   implicit object DurationFormat extends RootJsonFormat[Duration] {
      def write(d: Duration) = d.getMillis.toJson
      def read(value: JsValue) = new Duration(value.toString.toLong)
   }

   object AggregateFormat extends RootJsonFormat[AggregateGenerator] {
      def write(obj: AggregateGenerator) =
      {
         val name = obj.name.toJson
         val `type` = obj.`type`.toJson
         val aggregator = obj.aggregator.toJson
         val generators = obj.generators.map(g => g match {
            case Left(s) => s.toJson
            case Right(g) => g.toJson
         }).toJson

         new JsObject(Map(
            "name" -> name,
            "type" -> `type`,
            "aggregator" -> aggregator,
            "generators" -> generators
         ))
      }

      def read(value: JsValue) =
      {
         val fields = value.asJsObject.fields

         val name = if(fields contains "name") fields("name") match {
            case JsString(s) => Some(s)
            case _ => None
         }
                    else None

         val `type` = fields("type").convertTo[String]
         val aggregator = fields("aggregator").convertTo[String]
         val generators = fields("generators") match {
            case JsArray(x) => x.map(v => v match {
               case JsString(s) => Left(s)
               case g => Right(g.convertTo[Generator])
            })
         }

         AggregateGenerator(name, `type`, aggregator, generators)
      }
   }

   object CorrelatedFormat extends RootJsonFormat[CorrelatedGenerator]
   {
      def write(obj: CorrelatedGenerator) =
      {
         val name = obj.name.toJson
         val `type` = obj.`type`.toJson
         val generator = (obj.generator match {
            case Left(s) => s.toJson
            case Right(g) => g.toJson
         }).toJson
         val coef = obj.coef.toJson

         new JsObject(Map(
            "name" -> name,
            "type" -> `type`,
            "generator" -> generator,
            "coef" -> coef
         ))
      }

      def read(value: JsValue) =
      {
         val fields = value.asJsObject.fields

         val name = if(fields contains "name") fields("name") match {
            case JsString(s) => Some(s)
            case _ => None
         }
                    else None

         val `type` = fields("type").convertTo[String]
         val generator = fields("generator") match {
               case JsString(s) => Left(s)
               case g => Right(g.convertTo[Generator])
         }
         val coef = fields("coef").convertTo[Double]

         CorrelatedGenerator(name, `type`, generator, coef)
      }
   }

   object LogisticFormat extends RootJsonFormat[LogisticGenerator]
   {
      def write(obj: LogisticGenerator) =
      {
         val name = obj.name.toJson
         val `type` = obj.`type`.toJson
         val generator = (obj.generator match {
            case Left(s) => s.toJson
            case Right(g) => g.toJson
         }).toJson
         val location = obj.location.toJson
         val scale = obj.scale.toJson
         val seed = obj.seed.toJson

         new JsObject(Map(
            "name" -> name,
            "type" -> `type`,
            "generator" -> generator,
            "location" -> location,
            "scale" -> scale,
            "seed" -> seed
         ))
      }

      def read(value: JsValue) =
      {
         val fields = value.asJsObject.fields

         val name = if(fields contains "name") fields("name") match {
            case JsString(s) => Some(s)
            case _ => None
         }
                    else None

         val `type` = fields("type").convertTo[String]
         val generator = fields("generator") match {
            case JsString(s) => Left(s)
            case g => Right(g.convertTo[Generator])
         }
         val location = fields("location").convertTo[Double]
         val scale = fields("scale").convertTo[Double]
         val seed = fields.get("seed").map(_.convertTo[Long])

         LogisticGenerator(name, `type`, generator, location, scale, seed)
      }
   }

   implicit val generatorFormat = GeneratorFormat
   implicit val armaModelFormat = jsonFormat5(ARMAModel)
   implicit val armaFormat = jsonFormat4(ARMAGenerator)

   implicit val dailyFormat = jsonFormat3(DailyGenerator)
   implicit val weeklyFormat = jsonFormat3(WeeklyGenerator)
   implicit val monthlyFormat = jsonFormat3(MonthlyGenerator)
   implicit val yearlyFormat = jsonFormat3(YearlyGenerator)
   implicit val constantFormat = jsonFormat3(ConstantGenerator)
   implicit val functionFormat = lazyFormat(jsonFormat5(FunctionGenerator))
   implicit val aggregateFormat = lazyFormat(AggregateFormat)
   implicit val correlatedFormat = lazyFormat(CorrelatedFormat)
   implicit val logisticFormat = lazyFormat(LogisticFormat)
   implicit val transitionModelFormat = jsonFormat3(Transition)
   implicit val transitionFormat = lazyFormat(jsonFormat4(TransitionGenerator))
   implicit val limitedFormat = lazyFormat(jsonFormat6(LimitedGenerator))
   implicit val partialFormat = lazyFormat(jsonFormat5(PartialGenerator))
}

object GeneratorFormat extends JsonFormat[Generator]
{
   import GeneratorLeafFormat._

   def deserializationError(s: String): Generator =
   {
      println(s)
      InvalidGenerator(s)
   }

   def serializationError(s: String): JsValue =
   {
      println(s)
      JsString(s)
   }

   override def read(json: JsValue): Generator = json match {
      case known:JsObject if known.fields.contains("type") =>
         known.fields.get("type").get match{
            case JsString("arma") => armaFormat.read(known)
            case JsString("daily") => dailyFormat.read(known)
            case JsString("weekly") => weeklyFormat.read(known)
            case JsString("monthly") => monthlyFormat.read(known)
            case JsString("yearly") => yearlyFormat.read(known)
            case JsString("constant") => constantFormat.read(known)
            case JsString("aggregate") => aggregateFormat.read(known)
            case JsString("correlated") => correlatedFormat.read(known)
            case JsString("logistic") => logisticFormat.read(known)
            case unknown => deserializationError(s"unknown Generator object: ${unknown}")
         }
      case unknown => deserializationError(s"unknown  Generator object: ${unknown}")
   }


   override def write(obj: Generator): JsValue = obj match {
      case x: ARMAGenerator => armaFormat.write(x)
      case x: DailyGenerator => dailyFormat.write(x)
      case x: WeeklyGenerator => weeklyFormat.write(x)
      case x: MonthlyGenerator => monthlyFormat.write(x)
      case x: YearlyGenerator => yearlyFormat.write(x)
      case x: ConstantGenerator => constantFormat.write(x)
      case x: AggregateGenerator => aggregateFormat.write(x)
      case x: CorrelatedGenerator => correlatedFormat.write(x)
      case x: LogisticGenerator => logisticFormat.write(x)
      case unrecognized => serializationError(s"Serialization problem ${unrecognized}")
   }
}