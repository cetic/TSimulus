package be.cetic.tsgen.config

import be.cetic.tsgen._
import com.github.nscala_time.time.Imports._
import org.joda.time.{DateTimeConstants, LocalDateTime, LocalTime}
import spray.json._

import scala.util.Random


abstract class Generator[+T](val name: Option[String], val `type`: String)
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

class InvalidGenerator(name: String) extends Generator[Any](Some(name), "ERROR")
{
   override def timeseries(generators: String => Generator[Any]) = ???
}

case class Configuration(generators: Option[Seq[Generator[Any]]],
                         series: Seq[Series[Any]],
                         from: LocalDateTime,
                         to: LocalDateTime)
{
   /**
     * @return the final time series associated to the configuration files.
     *         A name is associated to each time series.
     */
   def timeSeries: Map[String, (TimeSeries[Any], Duration)] =
   {
      val memory = firstOrderGenerators

      series.map(s => {
         val duration = s.frequency
         val generator = Model.generator(memory)(s.generator)

         s.name -> (generator.timeseries(memory), duration)
      }).toMap
   }

   private def firstOrderGenerators: Map[String, Generator[Any]] =
   {
       generators match {
          case None => Map()
          case Some(gens) => {
             val memory = scala.collection.mutable.Map[String, Generator[Any]]()

             gens.foreach(g => {
                memory.put(g.name.get, g)
             })

             memory.toMap
          }
       }
   }
}

case class Series[T](name: String, generator: Either[String, Generator[Any]], frequency: Duration)

class ARMAGenerator(name: Option[String],
                         val model: ARMAModel,
                         val timestep: Duration) extends Generator[Double](name, "arma")
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

   override def toString() = "ARMAGenerator(" + model + "," + timestep + ")"

   override def equals(o: Any) = o match {
      case that: ARMAGenerator => that.name == this.name && that.model == this.model && that.timestep == this.timestep
      case _ => false
   }
}

case class ARMAModel(phi: Option[Seq[Double]],
                     theta: Option[Seq[Double]],
                     std: Double,
                     c: Double,
                     seed: Option[Long])

class DailyGenerator(name: Option[String],
                     val points: Map[LocalTime, Double]) extends Generator[Double](name, "daily")
{
   override def timeseries(generators: String => Generator[Any]) = DailyTimeSeries(points)

   override def toString() = "DailyGenerator(" + name + "," + points + ")"

   override def equals(o: Any) = o match {
      case that: DailyGenerator => that.name == this.name && that.points == this.points
      case _ => false
   }
}

class WeeklyGenerator(name: Option[String],
                      val points: Map[String, Double]) extends Generator[Double](name, "weekly")
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

   override def toString() = "WeeklyGenerator(" + name + "," + points + ")"

   override def equals(o: Any) = o match {
      case that: WeeklyGenerator => that.name == this.name && that.points == this.points
      case _ => false
   }
}

class MonthlyGenerator(name: Option[String],
                       val points: Map[String, Double]) extends Generator[Double](name, "monthly")
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

   override def toString() = "MonthlyGenerator(" + name + "," + points + ")"

   override def equals(o: Any) = o match {
      case that: MonthlyGenerator => that.name == this.name && that.points == this.points
      case _ => false
   }
}

class YearlyGenerator(name: Option[String],
                      val points: Map[Int, Double]) extends Generator[Double](name, "yearly")
{
   override def timeseries(generators: String => Generator[Any]) = YearlyTimeSeries(points)

   override def toString() = "YearlyGenerator(" + name + "," + points + ")"

   override def equals(o: Any) = o match {
      case that: YearlyGenerator => that.name == this.name && that.points == this.points
      case _ => false
   }
}

class ConstantGenerator(name: Option[String],
                        val value: Double) extends Generator[Double](name, "constant")
{
   override def timeseries(generators: String => Generator[Any]) = ConstantTimeSeries(value)

   override def toString() = "ConstantGenerator(" + name + "," + value + ")"

   override def equals(o: Any) = o match {
      case that: ConstantGenerator => that.name == this.name && that.value == this.value
      case _ => false
   }
}


class FunctionGenerator(name: Option[String],
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

   override def toString() = "FunctionGenerator(" + name + "," + generator + "," + slope + "," + intercept + ")"

   override def equals(o: Any) = o match {
      case that: FunctionGenerator => (that.name == this.name &&
         that.generator == this.generator &&
         that.slope == this.slope &&
         that.intercept == this.intercept)
      case _ => false
   }
}

class AggregateGenerator(name: Option[String],
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

   override def toString() = "AggregateGenerator(" + name + "," + aggregator + "," + generators + ")"

   override def equals(o: Any) = o match {
      case that: AggregateGenerator => that.name == this.name && that.aggregator == this.aggregator && that.generators == this.generators
      case _ => false
   }
}

class CorrelatedGenerator(name: Option[String],
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

   override def toString() = "CorrelatedGenerator(" + name + "," + generator + "," + "coef" + ")"

   override def equals(o: Any) = o match {
      case that: CorrelatedGenerator => that.name == this.name && that.generator == this.generator && that.coef == this.coef
      case _ => false
   }
}

class LogisticGenerator(name: Option[String],
                        val generator: Either[String, Generator[Any]],
                        val location: Double,
                        val scale: Double,
                        val seed: Option[Int]) extends Generator[Boolean](name, "logistic")
{

   override def timeseries(generators: (String) => Generator[Any]) =
   {
      Model.generator(generators)(generator).timeseries(generators) match {
         case dTS: TimeSeries[Double] => LogisticTimeSeries(dTS, location, scale, seed.getOrElse(Random.nextInt()))
         case other => throw new ClassCastException(other.toString)
      }
   }

   override def toString() = "LogisticGenerator(" + name + "," + generator + "," + location + "," + scale + "," + seed + ")"

   override def equals(o: Any) = o match {
      case that: LogisticGenerator => that.name == this.name &&
         that.generator == this.generator &&
         that.location == this.location &&
         that.scale == this.scale &&
         that.seed == this.seed
      case _ => false
   }
}

class TransitionGenerator(name: Option[String],
                          val origin: Either[String, Generator[Any]],
                          val transitions: Seq[Transition]) extends Generator(name, "transition")
{
   override def timeseries(generators: (String) => Generator[Any]) = ???

   override def toString() = "TransitionGenerator(" + name + "," + origin + "," + transitions + ")"

   override def equals(o: Any) = o match {
      case that: TransitionGenerator => that.name == this.name && that.origin == this.origin && that.transitions == this.transitions
      case _ => false
   }
}

case class Transition(generator: Either[String, Generator[Any]], start: LocalDateTime, delay: Option[Duration])

class LimitedGenerator(name: Option[String],
                       val generator: Either[String, Generator[Any]],
                       val from: Option[LocalDateTime],
                       val to: Option[LocalDateTime],
                       val missingRate: Option[Double]) extends Generator(name, "limited")
{
   override def timeseries(generators: (String) => Generator[Any]) = ???

   override def toString() = "LimitedGenerator(" + name + "," + generator + "," + from + "," + to + "," + missingRate + ")"

   override def equals(o: Any) = o match {
      case that: LimitedGenerator => that.name == this.name &&
         that.generator == this.generator &&
         that.from == this.from &&
         that.to == this.to &&
         that.missingRate == this.missingRate
      case _ => false
   }
}

class PartialGenerator(name: Option[String],
                       val generator: Either[String, Generator[Any]],
                       val from: Option[LocalDateTime],
                       val to: Option[LocalDateTime]) extends Generator(name, "partial")
{
   override def timeseries(generators: (String) => Generator[Any]) = ???

   override def toString() = "PartialGenerator(" + name + "," + generator + "," + from + "," + to

   override def equals(o: Any) = o match {
      case that: PartialGenerator => that.name == this.name &&
         that.generator == this.generator &&
         that.from == this.from &&
         that.to == this.to
      case _ => false
   }
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

   implicit object ARMAFormat extends RootJsonFormat[ARMAGenerator] {

      override def write(obj: ARMAGenerator): JsValue = {

         val t = Map(
            "type" -> obj.`type`.toJson,
            "model" -> obj.model.toJson,
            "timestep" -> obj.timestep.toJson
         )

         new JsObject(
            obj.name.map(n => t + ("name" -> n.toJson)).getOrElse(t)
         )
      }

      override def read(json: JsValue): ARMAGenerator = {
         val name = json.asJsObject.fields.get("name") .map(f => f match {
            case JsString(x) => x
         })

         val model = json.asJsObject.fields("model").convertTo[ARMAModel]
         val timestep = json.asJsObject.fields("timestep").convertTo[Duration]

         new ARMAGenerator(name, model, timestep)
      }
   }

   implicit object MonthlyFormat extends RootJsonFormat[MonthlyGenerator] {

      override def write(obj: MonthlyGenerator): JsValue = {
         val name = obj.name.toJson
         val points = obj.points map {case (k, v) => (k.toString, v)} toJson

         val t = Map(
            "type" -> obj.`type`.toJson,
            "points" -> obj.points.toJson
         )

         new JsObject(
            obj.name.map(n => t + ("name" -> n.toJson)).getOrElse(t)
         )
      }

      override def read(json: JsValue): MonthlyGenerator = {
         val name = json.asJsObject.fields.get("name") .map(f => f match {
            case JsString(x) => x
         })

         val points = json.asJsObject.fields("points") match {
            case JsObject(x) => x
            case _ => throw new ClassCastException
         }

         val r = points map { case (k,v) => (k, v match { case JsNumber(x) => x.toDouble })}

         new MonthlyGenerator(name, r)
      }
   }

   implicit object YearlyFormat extends RootJsonFormat[YearlyGenerator] {

      override def write(obj: YearlyGenerator): JsValue = {
         val name = obj.name.toJson
         val points = obj.points map {case (k, v) => (k.toString, v)} toJson

         val t = Map(
            "type" -> obj.`type`.toJson,
            "points" -> obj.points.map { case (y,v) => (y.toString, v)}.toJson
         )

         new JsObject(
            obj.name.map(n => t + ("name" -> n.toJson)).getOrElse(t)
         )
      }

      override def read(json: JsValue): YearlyGenerator = {
         val name = json.asJsObject.fields.get("name") .map(f => f match {
            case JsString(x) => x
         })

         val points = json.asJsObject.fields("points") match {
            case JsObject(x) => x
            case _ => throw new ClassCastException
         }

         val r = points map { case (k,v) => (k.toInt, v match { case JsNumber(x) => x.toDouble })}

         new YearlyGenerator(name, r)
      }
   }

   implicit object ConstantFormat extends RootJsonFormat[ConstantGenerator] {

      override def write(obj: ConstantGenerator): JsValue = {
         val t = Map(
            "type" -> obj.`type`.toJson,
            "value" -> obj.value.toJson
         )

         new JsObject(
            obj.name.map(n => t + ("name" -> n.toJson)).getOrElse(t)
         )
      }

      override def read(json: JsValue): ConstantGenerator = {

         val fields = json.asJsObject.fields
         val name = fields.get("name").map(f => f match {
            case JsString(x) => x
         })

         val value = fields("value") match {
            case JsNumber(n) => n.toDouble
         }

         new ConstantGenerator(name, value)
      }
   }

   object FunctionFormat extends RootJsonFormat[FunctionGenerator] {

      override def write(obj: FunctionGenerator): JsValue = {

         val generator = (obj.generator match {
            case Left(s) => s.toJson
            case Right(g) => GeneratorFormat.write(g)
         }).toJson

         val t = Map(
            "type" -> obj.`type`.toJson,
            "generator" -> generator,
            "slope" -> obj.slope.toJson,
            "intercept" -> obj.intercept.toJson
         )

         new JsObject(
            obj.name.map(n => t + ("name" -> n.toJson)).getOrElse(t)
         )
      }

      override def read(json: JsValue): FunctionGenerator = {

         val fields = json.asJsObject.fields

         val name = json.asJsObject.fields.get("name") .map(f => f match {
            case JsString(x) => x
         })

         val generator = fields("generator") match {
            case JsString(s) => Left(s)
            case g => Right(GeneratorFormat.read(g))
         }

         val slope = fields("slope") match {
            case JsNumber(n) => n.toDouble
         }

         val intercept = fields("intercept") match {
            case JsNumber(n) => n.toDouble
         }

         new FunctionGenerator(name, generator, slope, intercept)
      }
   }

   implicit object DurationFormat extends RootJsonFormat[Duration] {
      def write(d: Duration) = d.getMillis.toJson
      def read(value: JsValue) = new Duration(value.toString.toLong)
   }

   implicit object DailyFormat extends RootJsonFormat[DailyGenerator] {
      def write(obj: DailyGenerator) =
      {
         val t = Map(
            "type" -> obj.`type`.toJson,
            "points" -> obj.points.toJson
         )

         new JsObject(
            obj.name.map(n => t + ("name" -> n.toJson)).getOrElse(t)
         )
      }

      def read(value: JsValue) =
      {
         val fields = value.asJsObject.fields

         val name = fields.get("name") .map(f => f match {
            case JsString(x) => x
         })

         val points = fields("points") match {
            case JsObject(x) => x
            case _ => throw new ClassCastException
         }

         val r = points map { case (k,v) => (ttf.parseLocalTime(k), v match { case JsNumber(x) => x.toDouble })}

         new DailyGenerator(name, r)
      }
   }

   implicit object WeeklyFormat extends RootJsonFormat[WeeklyGenerator] {
      def write(obj: WeeklyGenerator) =
      {
         val t = Map(
            "type" -> obj.`type`.toJson,
            "points" -> obj.points.toJson
         )

         new JsObject(
            obj.name.map(n => t + ("name" -> n.toJson)).getOrElse(t)
         )
      }

      def read(value: JsValue) =
      {
         val fields = value.asJsObject.fields

         val name = fields.get("name") .map(f => f match {
            case JsString(x) => x
         })

         val points = value.asJsObject.fields("points") match {
            case JsObject(x) => x
            case _ => throw new ClassCastException
         }

         val r = points map { case (k,v) => (k, v match { case JsNumber(x) => x.toDouble })}

         new WeeklyGenerator(name, r)
      }
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

         val t = Map(
            "type" -> obj.`type`.toJson,
            "aggregator" -> aggregator,
            "generators" -> generators
         )

         new JsObject(
            obj.name.map(n => t + ("name" -> n.toJson)).getOrElse(t)
         )
      }

      def read(value: JsValue) =
      {
         val fields = value.asJsObject.fields

         val name = fields.get("name") .map(f => f match {
            case JsString(x) => x
         })

         val aggregator = fields("aggregator").convertTo[String]
         val generators = fields("generators") match {
            case JsArray(x) => x.map(v => v match {
               case JsString(s) => Left(s)
               case g => Right(GeneratorFormat.read(g))
            }).toList
         }

         new AggregateGenerator(name, aggregator, generators)
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

         val t = Map(
            "type" -> obj.`type`.toJson,
            "generator" -> generator,
            "coef" -> coef
         )

         new JsObject(
            obj.name.map(n => t + ("name" -> n.toJson)).getOrElse(t)
         )
      }

      def read(value: JsValue) =
      {
         val fields = value.asJsObject.fields

         val name = fields.get("name") .map(f => f match {
            case JsString(x) => x
         })

         val `type` = fields("type").convertTo[String]
         val generator = fields("generator") match {
               case JsString(s) => Left(s)
               case g => Right(GeneratorFormat.read(g))
         }
         val coef = fields("coef").convertTo[Double]

         new CorrelatedGenerator(name, generator, coef)
      }
   }

   object LogisticFormat extends RootJsonFormat[LogisticGenerator]
   {
      def write(obj: LogisticGenerator) =
      {
         val generator = (obj.generator match {
            case Left(s) => s.toJson
            case Right(g) => GeneratorFormat.write(g)
         }).toJson
         val location = obj.location.toJson
         val scale = obj.scale.toJson
         val seed = obj.seed.toJson

         val t = Map(
            "type" -> obj.`type`.toJson,
            "generator" -> generator,
            "location" -> location,
            "scale" -> scale,
            "seed" -> seed
         )

         new JsObject(
            obj.name.map(n => t + ("name" -> n.toJson)).getOrElse(t)
         )
      }

      def read(value: JsValue) =
      {
         val fields = value.asJsObject.fields

         val name = fields.get("name") .map(f => f match {
            case JsString(x) => x
         })

         val generator = fields("generator") match {
            case JsString(s) => Left(s)
            case g => Right(GeneratorFormat.read(g))
         }
         val location = fields("location").convertTo[Double]
         val scale = fields("scale").convertTo[Double]
         val seed = fields.get("seed").map(_.convertTo[Int])

         new LogisticGenerator(name, generator, location, scale, seed)
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
         val origin = (obj.origin match {
            case Left(s) => s.toJson
            case Right(g) => GeneratorFormat.write(g)
         }).toJson
         val transitions = obj.transitions.toJson

         val t = Map(
            "type" -> obj.`type`.toJson,
            "origin" -> origin,
            "transitions" -> transitions
         )

         new JsObject(
            obj.name.map(n => t + ("name" -> n.toJson)).getOrElse(t)
         )
      }

      def read(value: JsValue) =
      {
         val fields = value.asJsObject.fields

         val name = fields.get("name") .map(f => f match {
            case JsString(x) => x
         })

         val origin = fields("origin") match {
            case JsString(s) => Left(s)
            case g => Right(GeneratorFormat.read(g))
         }
         val transitions = fields("transitions").convertTo[Seq[Transition]]

         new TransitionGenerator(name, origin, transitions)
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

         val t = Map(
            "type" -> obj.`type`.toJson,
            "generator" -> generator,
            "from" -> from,
            "to" -> to,
            "missing-rate" -> missingRate
         )

         new JsObject(
            obj.name.map(n => t + ("name" -> n.toJson)).getOrElse(t)
         )
      }

      def read(value: JsValue) =
      {
         val fields = value.asJsObject.fields

         val name = fields.get("name") .map(f => f match {
            case JsString(x) => x
         })

         val generator = fields("generator") match {
            case JsString(s) => Left(s)
            case g => Right(GeneratorFormat.read(g))
         }
         val from = fields.get("from").map(_.convertTo[LocalDateTime])
         val to = fields.get("to").map(_.convertTo[LocalDateTime])
         val missingRate = fields.get("missing-rate").map(_.convertTo[Double])

         new LimitedGenerator(name, generator, from, to, missingRate)
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

         val t = Map(
            "type" -> obj.`type`.toJson,
            "generator" -> generator,
            "from" -> from,
            "to" -> to
         )

         new JsObject(
            obj.name.map(n => t + ("name" -> n.toJson)).getOrElse(t)
         )
      }

      def read(value: JsValue) =
      {
         val fields = value.asJsObject.fields

         val name = fields.get("name") .map(f => f match {
            case JsString(x) => x
         })

         val generator = fields("generator") match {
            case JsString(s) => Left(s)
            case g => Right(GeneratorFormat.read(g))
         }
         val from = fields.get("from").map(_.convertTo[LocalDateTime])
         val to = fields.get("to").map(_.convertTo[LocalDateTime])

         new PartialGenerator(name, generator, from, to)
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
            "name" -> obj.name.toJson,
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

         val name = fields("name").convertTo[String]

         Series(name, generator, frequency)
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

   implicit val functionFormat = lazyFormat(FunctionFormat)
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

   def deserializationError(s: String): Generator[Any] = throw new DeserializationException(s)


   def serializationError(s: String): JsValue = throw new SerializationException(s)

   override def read(json: JsValue): Generator[Any] = json match {
      case known:JsObject if known.fields.contains("type") =>
         known.fields.get("type").get match{
            case JsString("arma") => ARMAFormat.read(known)
            case JsString("daily") => DailyFormat.read(known)
            case JsString("weekly") => WeeklyFormat.read(known)
            case JsString("monthly") => MonthlyFormat.read(known)
            case JsString("yearly") => YearlyFormat.read(known)
            case JsString("constant") => ConstantFormat.read(known)
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
      case x: ARMAGenerator => ARMAFormat.write(x)
      case x: DailyGenerator => DailyFormat.write(x)
      case x: WeeklyGenerator => WeeklyFormat.write(x)
      case x: MonthlyGenerator => MonthlyFormat.write(x)
      case x: YearlyGenerator => YearlyFormat.write(x)
      case x: ConstantGenerator => ConstantFormat.write(x)
      case x: AggregateGenerator => aggregateFormat.write(x)
      case x: CorrelatedGenerator => correlatedFormat.write(x)
      case x: LogisticGenerator => logisticFormat.write(x)
      case x: TransitionGenerator => transitionFormat.write(x)
      case x: LimitedGenerator => limitedFormat.write(x)
      case x: PartialGenerator => partialFormat.write(x)
      case unrecognized => serializationError(s"Serialization problem ${unrecognized}")
   }
}