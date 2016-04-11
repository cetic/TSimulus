package be.cetic.tsgen.config

import be.cetic.tsgen._
import com.github.nscala_time.time.Imports._
import org.joda.time.{DateTimeConstants, LocalDateTime, LocalTime}
import spray.json._

import scala.util.Random


abstract class Generator[+T](name: Option[String], `type`: String)
{
   /**
     * @param generators an entity able to discover all the first-level generators on the
     *                   basis of their name.
     * @return the time series associated to this generator
     */
   def timeseries(generators: String => Generator[Any]): TimeSeries[T]
}

object Model
{
   def generator(generators: String => Generator[Any])(element: Either[String, Generator[Any]]): Generator[Any] =
      element match {
         case Left(s) => generators(s)
         case Right(g) => g
      }
}

case class InvalidGenerator(name: String) extends Generator[Any](Some(name), "ERROR")
{
   override def timeseries(generators: String => Generator[Any]) = ???
}

case class Configuration(generators: Option[Seq[Generator[Any]]],
                         series: Seq[Series[Any]],
                         from: LocalDateTime,
                         to: LocalDateTime)

case class Series[T](generator: Either[String, Generator[Any]], frequency: Duration)

case class ARMAGenerator(name: Option[String],
                         `type`: String,
                         val model: ARMAModel,
                         val timestep: Duration) extends Generator[Double](name, `type`)
{
   override def timeseries(generators: String => Generator[Any]) =
      RandomWalkTimeSeries(
         new ARMA(
            model.phi.getOrElse(Seq()).toArray,
            model.theta.getOrElse(Seq()).toArray,
            model.std,
            model.c,
            model.seed.getOrElse(Random.nextLong())
         ),
         timestep
      )
}

case class ARMAModel(phi: Option[Seq[Double]],
                     theta: Option[Seq[Double]],
                     std: Double,
                     c: Double,
                     seed: Option[Long])

case class DailyGenerator(name: Option[String],
                          `type`: String,
                          val points: Map[LocalTime, Double]) extends Generator[Double](name, `type`)
{
   override def timeseries(generators: String => Generator[Any]) = DailyTimeSeries(points)
}

case class WeeklyGenerator(name: Option[String],
                           `type`: String,
                           val points: Map[String, Double]) extends Generator[Double](name, `type`)
{
   override def timeseries(generators: String => Generator[Any]) =
   {
      val day = Map(
         "monday" -> DateTimeConstants.MONDAY,
         "tuesday" -> DateTimeConstants.TUESDAY,
         "wednesday" -> DateTimeConstants.WEDNESDAY,
         "thurdsay" -> DateTimeConstants.THURSDAY,
         "friday" -> DateTimeConstants.FRIDAY,
         "saturday" -> DateTimeConstants.SATURDAY,
         "sunday" -> DateTimeConstants.SUNDAY
      )

      WeeklyTimeSeries(points map {case (k,v) => (day(k), v)})
   }
}

case class MonthlyGenerator(name: Option[String],
                            `type`: String,
                            val points: Map[String, Double]) extends Generator[Double](name, `type`)
{
   override def timeseries(generators: String => Generator[Any]) =
   {
      val month = Map(
         "january" -> DateTimeConstants.JANUARY,
         "february" -> DateTimeConstants.FEBRUARY,
         "march" -> DateTimeConstants.MARCH,
         "april" -> DateTimeConstants.APRIL,
         "may" -> DateTimeConstants.MAY,
         "june" -> DateTimeConstants.JUNE,
         "july" -> DateTimeConstants.JULY,
         "august" -> DateTimeConstants.AUGUST,
         "september" -> DateTimeConstants.SEPTEMBER,
         "october" -> DateTimeConstants.OCTOBER,
         "december" -> DateTimeConstants.DECEMBER
      )

      MonthlyTimeSeries(points map {case (k,v) => (month(k), v)})
   }
}

case class YearlyGenerator(name: Option[String],
                           `type`: String,
                           val points: Map[Int, Double]) extends Generator[Double](name, `type`)
{
   override def timeseries(generators: String => Generator[Any]) = YearlyTimeSeries(points)
}

case class ConstantGenerator(name: Option[String],
                             `type`: String,
                             val value: Double) extends Generator[Double](name, `type`)
{
   override def timeseries(generators: String => Generator[Any]) = ConstantTimeSeries(value)
}


case class FunctionGenerator(name: Option[String],
                             val `type`: String,
                             val generator: Either[String, Generator[Any]],
                             val slope: Double,
                             val intercept: Double) extends Generator[Double](name, "function")
{
   override def timeseries(generators: String => Generator[Any]) =
   {
      Model.generator(generators)(generator) match {
         case g: Generator[Double] => FunctionTimeSeries[Double](g.timeseries(generators), x => Some(slope * x + intercept))
         case _ => throw new ClassCastException
      }
   }
}

case class AggregateGenerator(name: Option[String],
                              val aggregator: String,
                              val generators: Seq[Either[String, Generator[Any]]]) extends Generator[Double](name, "aggregate")
{
   override def timeseries(gen: String => Generator[Any]) =
   {
      val agg = aggregator match {
         case "sum" => s: Seq[Double] => s.sum
         case "product" => s: Seq[Double] => s.reduce((x,y) => x*y)
         case "min" => s: Seq[Double] => s.min
         case "max" => s: Seq[Double] => s.max
         case "mean" => s: Seq[Double] => s.sum / s.length
      }

      val ts = generators.map(x => x match {
         case Left(s) => gen(s).timeseries(gen)
         case Right(g) => g.timeseries(gen)
      })

      val series = ts flatMap {
         case d : TimeSeries[Double] => Some(d)
         case _ => None
      }

      new CompositeTimeSeries[Double](agg, series)
   }
}

case class CorrelatedGenerator(name: Option[String],
                               val generator: Either[String, Generator[Any]],
                               val coef: Double) extends Generator[Double](name, "correlated")
{
   override def timeseries(generators: (String) => Generator[Any]) =
   {
      Model.generator(generators)(generator) match {
         case dDouble : Generator[Double] => CorrelatedTimeSeries(dDouble.timeseries(generators), Random.nextInt(), coef)
         case _ => throw new ClassCastException
      }
   }
}

case class LogisticGenerator(name: Option[String],
                             val generator: Either[String, Generator[Any]],
                             val location: Double,
                             val scale: Double,
                             val seed: Option[Int]) extends Generator[Boolean](name, "logistic")
{

   override def timeseries(generators: (String) => Generator[Any]) =
   {
      Model.generator(generators)(generator) match {
         case dTS: TimeSeries[Double] => LogisticTimeSeries(dTS, location, scale, seed.getOrElse(Random.nextInt()))
         case _ => throw new ClassCastException
      }
   }
}

case class TransitionGenerator(name: Option[String],
                               val origin: Either[String, Generator[Any]],
                               val transitions: Seq[Transition]) extends Generator(name, "transition")
{
   override def timeseries(generators: (String) => Generator[Any]) = ???
}

case class Transition(generator: Either[String, Generator[Any]], start: LocalDateTime, delay: Option[Duration])

case class LimitedGenerator(name: Option[String],
                            val generator: Either[String, Generator[Any]],
                            val from: Option[LocalDateTime],
                            val to: Option[LocalDateTime],
                            val missingRate: Option[Double]) extends Generator(name, "limited")
{
   override def timeseries(generators: (String) => Generator[Any]) = ???
}

case class PartialGenerator(name: Option[String],
                            val generator: Either[String, Generator[Any]],
                            val from: Option[LocalDateTime],
                            val to: Option[LocalDateTime]) extends Generator(name, "partial")
{
   override def timeseries(generators: (String) => Generator[Any]) = ???
}

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
            case _ => throw new ClassCastException
         }

         val points = json.asJsObject.fields("points") match {
            case JsObject(x) => x
            case _ => throw new ClassCastException
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
         val aggregator = obj.aggregator.toJson
         val generators = obj.generators.map(g => g match {
            case Left(s) => s.toJson
            case Right(x) => GeneratorFormat.write(x)
         }).toJson

         new JsObject(Map(
            "name" -> name,
            "type" -> "aggregate".toJson,
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

         val aggregator = fields("aggregator").convertTo[String]
         val generators = fields("generators") match {
            case JsArray(x) => x.map(v => v match {
               case JsString(s) => Left(s)
               case g => Right(GeneratorFormat.read(g))
            }).toList
         }

         AggregateGenerator(name, aggregator, generators)
      }
   }

   object CorrelatedFormat extends RootJsonFormat[CorrelatedGenerator]
   {
      def write(obj: CorrelatedGenerator) =
      {
         val name = obj.name.toJson
         val generator = (obj.generator match {
            case Left(s) => s.toJson
            case Right(g) => GeneratorFormat.write(g)
         }).toJson
         val coef = obj.coef.toJson

         new JsObject(Map(
            "name" -> name,
            "type" -> "correlated".toJson,
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
               case g => Right(GeneratorFormat.read(g))
         }
         val coef = fields("coef").convertTo[Double]

         CorrelatedGenerator(name, generator, coef)
      }
   }

   object LogisticFormat extends RootJsonFormat[LogisticGenerator]
   {
      def write(obj: LogisticGenerator) =
      {
         val name = obj.name.toJson
         val generator = (obj.generator match {
            case Left(s) => s.toJson
            case Right(g) => GeneratorFormat.write(g)
         }).toJson
         val location = obj.location.toJson
         val scale = obj.scale.toJson
         val seed = obj.seed.toJson

         new JsObject(Map(
            "name" -> name,
            "type" -> "logistic".toJson,
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

         val generator = fields("generator") match {
            case JsString(s) => Left(s)
            case g => Right(GeneratorFormat.read(g))
         }
         val location = fields("location").convertTo[Double]
         val scale = fields("scale").convertTo[Double]
         val seed = fields.get("seed").map(_.convertTo[Int])

         LogisticGenerator(name, generator, location, scale, seed)
      }
   }

   implicit object TransitionModelFormat extends RootJsonFormat[Transition]
   {
      def write(obj: Transition) =
      {
         val generator = (obj.generator match {
            case Left(s) => s.toJson
            case Right(g) => GeneratorFormat.write(g)
         }).toJson
         val start = obj.start.toJson
         val delay = obj.delay.toJson

         new JsObject(Map(
            "generator" -> generator,
            "start" -> start,
            "delay" -> delay
         ))
      }

      def read(value: JsValue) =
      {
         val fields = value.asJsObject.fields

         val generator = fields("generator") match {
            case JsString(s) => Left(s)
            case g => Right(GeneratorFormat.read(g))
         }
         val start = fields("start").convertTo[LocalDateTime]
         val delay = fields.get("delay").map(_.convertTo[Duration])

         Transition(generator, start, delay)
      }
   }

   object TransitionFormat extends RootJsonFormat[TransitionGenerator]
   {
      def write(obj: TransitionGenerator) =
      {
         val name = obj.name.toJson
         val origin = (obj.origin match {
            case Left(s) => s.toJson
            case Right(g) => GeneratorFormat.write(g)
         }).toJson
         val transitions = obj.transitions.toJson

         new JsObject(Map(
            "name" -> name,
            "type" -> "transition".toJson,
            "origin" -> origin,
            "transitions" -> transitions
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

         val origin = fields("origin") match {
            case JsString(s) => Left(s)
            case g => Right(GeneratorFormat.read(g))
         }
         val transitions = fields("transitions").convertTo[Seq[Transition]]

         TransitionGenerator(name, origin, transitions)
      }
   }

   object LimitedFormat extends RootJsonFormat[LimitedGenerator]
   {
      def write(obj: LimitedGenerator) =
      {
         val name = obj.name.toJson
         val generator = (obj.generator match {
            case Left(s) => s.toJson
            case Right(g) => GeneratorFormat.write(g)
         }).toJson
         val from = obj.from.toJson
         val to = obj.to.toJson
         val missingRate = obj.missingRate.toJson

         new JsObject(Map(
            "name" -> name,
            "type" -> "limited".toJson,
            "generator" -> generator,
            "from" -> from,
            "to" -> to,
            "missing-rate" -> missingRate
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

         val generator = fields("generator") match {
            case JsString(s) => Left(s)
            case g => Right(GeneratorFormat.read(g))
         }
         val from = fields.get("from").map(_.convertTo[LocalDateTime])
         val to = fields.get("to").map(_.convertTo[LocalDateTime])
         val missingRate = fields.get("missing-rate").map(_.convertTo[Double])

         LimitedGenerator(name, generator, from, to, missingRate)
      }
   }

   object PartialFormat extends RootJsonFormat[PartialGenerator]
   {
      def write(obj: PartialGenerator) =
      {
         val name = obj.name.toJson
         val generator = (obj.generator match {
            case Left(s) => s.toJson
            case Right(g) => GeneratorFormat.write(g)
         }).toJson
         val from = obj.from.toJson
         val to = obj.to.toJson

         new JsObject(Map(
            "name" -> name,
            "type" -> "partial".toJson,
            "generator" -> generator,
            "from" -> from,
            "to" -> to
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

         val generator = fields("generator") match {
            case JsString(s) => Left(s)
            case g => Right(GeneratorFormat.read(g))
         }
         val from = fields.get("from").map(_.convertTo[LocalDateTime])
         val to = fields.get("to").map(_.convertTo[LocalDateTime])

         PartialGenerator(name, generator, from, to)
      }
   }

   implicit object SeriesFormat extends RootJsonFormat[Series[Any]]
   {
      def write(obj: Series[Any]) =
      {
         val generator = (obj.generator match {
            case Left(s) => s.toJson
            case Right(g) => GeneratorFormat.write(g)
         }).toJson
         val frequency = obj.frequency.toJson

         new JsObject(Map(
            "generator" -> generator,
            "frequency" -> frequency
         ))
      }

      def read(value: JsValue) =
      {
         val fields = value.asJsObject.fields

         val generator = fields("generator") match {
            case JsString(s) => Left(s)
            case g => Right(GeneratorFormat.read(g))
         }
         val frequency = fields("frequency").convertTo[Duration]

         Series(generator, frequency)
      }
   }

   implicit object ConfigurationFormat extends RootJsonFormat[Configuration]
   {
      def write(obj: Configuration) =
      {
         new JsObject(Map(
            "generators" -> obj.generators.map(g => g.map(GeneratorFormat.write(_))).toJson,
            "series" -> obj.series.toJson,
            "from" -> obj.from.toJson,
            "to" -> obj.to.toJson
         ))
      }

      def read(value: JsValue) =
      {
         val fields = value.asJsObject.fields

         val generators = fields.get("generators").map(gens => gens match {
            case JsArray(l) => l.map(GeneratorFormat.read(_))
            case _ => throw new ClassCastException
         })

         val series = fields("series") match {
            case JsArray(x) => x.map(_.convertTo[Series[Any]]).toSeq
            case _ => throw new ClassCastException
         }

         val from = fields("from").convertTo[LocalDateTime]
         val to = fields("to").convertTo[LocalDateTime]

         Configuration(generators, series, from, to)
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
   implicit val transitionFormat = lazyFormat(TransitionFormat)
   implicit val limitedFormat = lazyFormat(LimitedFormat)
   implicit val partialFormat = lazyFormat(PartialFormat)
}

object GeneratorFormat extends JsonFormat[Generator[Any]]
{
   import GeneratorLeafFormat._

   def deserializationError(s: String): Generator[Any] =
   {
      println(s)
      InvalidGenerator(s)
   }

   def serializationError(s: String): JsValue =
   {
      println(s)
      JsString(s)
   }

   override def read(json: JsValue): Generator[Any] = json match {
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
            case JsString("transition") => transitionFormat.read(known)
            case JsString("limited") => limitedFormat.read(known)
            case JsString("partial") => partialFormat.read(known)
            case unknown => deserializationError(s"unknown Generator object: ${unknown}")
         }
      case unknown => deserializationError(s"unknown  Generator object: ${unknown}")
   }

   override def write(obj: Generator[Any]): JsValue = obj match {
      case x: ARMAGenerator => armaFormat.write(x)
      case x: DailyGenerator => dailyFormat.write(x)
      case x: WeeklyGenerator => weeklyFormat.write(x)
      case x: MonthlyGenerator => monthlyFormat.write(x)
      case x: YearlyGenerator => yearlyFormat.write(x)
      case x: ConstantGenerator => constantFormat.write(x)
      case x: AggregateGenerator => aggregateFormat.write(x)
      case x: CorrelatedGenerator => correlatedFormat.write(x)
      case x: LogisticGenerator => logisticFormat.write(x)
      case x: TransitionGenerator => transitionFormat.write(x)
      case x: LimitedGenerator => limitedFormat.write(x)
      case x: PartialGenerator => partialFormat.write(x)
      case unrecognized => serializationError(s"Serialization problem ${unrecognized}")
   }
}