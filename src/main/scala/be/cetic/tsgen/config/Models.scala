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

   implicit object DurationFormat extends RootJsonFormat[Duration] {
      def write(d: Duration) = d.getMillis.toJson
      def read(value: JsValue) = new Duration(value.toString.toLong)
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
   implicit val aggregateFormat = lazyFormat(jsonFormat4(AggregateGenerator))
   implicit val correlatedFormat = lazyFormat(jsonFormat4(CorrelatedGenerator))
   implicit val logisticFormat = lazyFormat(jsonFormat6(LogisticGenerator))
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
            case unknown => deserializationError(s"unknown Generator object: ${unknown}")
         }
      case unknown => deserializationError(s"unknown  Generator object: ${unknown}")
   }


   override def write(obj: Generator): JsValue = obj match {
      case x: ARMAGenerator => armaFormat.write(x)
      case x: DailyGenerator => dailyFormat.write(x)
      case x: WeeklyGenerator => weeklyFormat.write(x)
      case unrecognized => serializationError(s"Serialization problem ${unrecognized}")
   }
}