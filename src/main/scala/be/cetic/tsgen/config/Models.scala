package be.cetic.tsgen.config

import be.cetic.tsgen._
import be.cetic.tsgen.timeseries._
import be.cetic.tsgen.timeseries.binary._
import be.cetic.tsgen.timeseries.composite._
import be.cetic.tsgen.timeseries.primary._
import com.github.nscala_time.time.Imports._
import org.apache.commons.math3.stat.StatUtils
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
         // Could also be expressed as a Sum(Times(generator, Constant(slope), intercept)
         case g: Generator[Double] => FunctionTimeSeries[Double](g.timeseries(generators), (t,v) => Some(slope * v + intercept))
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
      import scala.collection.JavaConversions._

      val agg = aggregator match {
         case "sum" => s: Seq[Double] => s.sum
         case "product" => s: Seq[Double] => s.reduce((x,y) => x*y)
         case "min" => s: Seq[Double] => s.min
         case "max" => s: Seq[Double] => s.max
         case "mean" => s: Seq[Double] => s.sum / s.length
         case "median" => s: Seq[Double] => StatUtils.percentile(s.toArray, 50);
      }

      val ts = generators.map(x => x match {
         case Left(s) => gen(s).timeseries(gen)
         case Right(g) => g.timeseries(gen)
      })

      val series = ts flatMap {
         case d : TimeSeries[Double] => Some(d)
         case _ => None
      }

      new AggregationTimeSeries[Double](agg, series)
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


class TrueGenerator(name: Option[String]) extends Generator[Boolean](name, "true")
{

   override def timeseries(generators: (String) => Generator[Any]) =
   {
      new TrueTimeSeries()
   }

   override def toString() = "TrueGenerator(" + name + ")"

   override def equals(o: Any) = o match {
      case that: TrueGenerator => that.name == this.name
      case _ => false
   }
}

class FalseGenerator(name: Option[String]) extends Generator[Boolean](name, "false")
{

   override def timeseries(generators: (String) => Generator[Any]) = new FalseTimeSeries()

   override def toString() = "FalseGenerator(" + name + ")"

   override def equals(o: Any) = o match {
      case that: FalseGenerator => that.name == this.name
      case _ => false
   }
}



class ConditionalGenerator(name: Option[String],
                           val condition: Either[String, Generator[Any]],
                           val success: Either[String, Generator[Any]],
                           val failure: Option[Either[String, Generator[Any]]]) extends Generator[Any](name, "conditional")
{

   override def timeseries(generators: (String) => Generator[Any]) =
   {
      val cond = Model.generator(generators)(condition).timeseries(generators) match {
         case t: TimeSeries[Boolean] => t
      }

      val a = Model.generator(generators)(success).timeseries(generators) match {
         case t: TimeSeries[Any] => t
      }


      val b = failure.map(f => Model.generator(generators)(f).timeseries(generators) match {
         case t: TimeSeries[Any] => t
      }).getOrElse(new UndefinedTimeSeries())
      
      ConditionalTimeSeries(cond, a, b)
   }

   override def toString() = "ConditionalGenerator(" + name + "," + condition + "," + success + "," + failure + ")"

   override def equals(o: Any) = o match {
      case that: ConditionalGenerator =>  that.name == this.name &&
                                          that.condition == this.condition &&
                                          that.success == this.success &&
                                          that.failure == this.failure
      case _ => false
   }
}

class TransitionGenerator(name: Option[String],
                          val first: Either[String, Generator[Any]],
                          val second: Either[String, Generator[Any]],
                          val time: LocalDateTime,
                          val interval: Option[Duration],
                          val f: Option[String]) extends Generator[Double](name, "transition")
{
   override def timeseries(generators: (String) => Generator[Any]) = {

      def interpolation = (a: Double, b: Double, ratio: Double) => a*(1-ratio) + ratio*b

      def smooth(x: Double) = 3*x*x - 2*x*x*x // smooth is ~= cossig
      def cossig(x: Double) = (1 - math.cos(math.Pi*x)) / 2
      def exp(x: Double) = Math.expm1(x) / (Math.E - 1)

      def linear = (a: Double, b: Double, ratio: Double) => interpolation(a,b,ratio)
      def sigmoid = (a: Double, b: Double, ratio: Double) => interpolation(a,b,cossig(ratio))
      def superlin = (a: Double, b: Double, ratio: Double) => interpolation(a,b,exp(ratio))

      val transition = f match {
         case Some(s) => {
            s match {
               case "linear" => linear
               case "sigmoid" => sigmoid
               case "exp" => superlin
               case _ => linear
            }
         }
         case None => linear
      }

      val firstBase = Model.generator(generators)(first).timeseries(generators) match {
         case t: TimeSeries[Double] => t
      }

      val secondBase = Model.generator(generators)(second).timeseries(generators) match {
         case t: TimeSeries[Double] => t
      }

      val t = interval.map(x =>  (x, transition))

      TransitionTimeSeries[Double](firstBase, secondBase, time, t)
   }

   override def toString() = "TransitionGenerator(" + name + "," + first + "," + second + "," + time + "," + interval + ")"

   override def equals(o: Any) = o match {
      case that: TransitionGenerator => that.name == this.name &&
         that.first == this.first &&
         that.second == this.second &&
         that.time == this.time &&
         that.interval == this.interval
      case _ => false
   }
}

class ThresholdGenerator(name: Option[String],
                         val generator: Either[String, Generator[Any]],
                         val threshold: Double,
                         val included: Option[Boolean]) extends Generator[Any](name, "threshold")
{
   override def timeseries(generators: (String) => Generator[Any]) =
   {
      val base = Model.generator(generators)(generator).timeseries(generators) match {
         case t: TimeSeries[Double] => t
      }

      val predicate = included match {
         case Some(false) => (x: Double) => x > threshold
         case _ => (x: Double) => x >= threshold
      }

      ArbitraryBinaryTimeSeries(base, predicate)
   }

   override def toString() = "ThresholdGenerator(" + name + "," + generator + "," + threshold + "," + included + ")"

   override def equals(o: Any) = o match {
      case that: ThresholdGenerator => that.name == this.name &&
         that.generator == this.generator &&
         that.threshold == this.threshold &&
         that.included == this.included
      case _ => false
   }
}

class AndGenerator(name: Option[String],
                   val a: Either[String, Generator[Any]],
                   val b: Either[String, Generator[Any]]) extends Generator[Any](name, "and")
{
   override def timeseries(generators: (String) => Generator[Any]) =
   {
      val first = Model.generator(generators)(a).timeseries(generators) match {
         case t: TimeSeries[Boolean] => t
      }

      val second = Model.generator(generators)(b).timeseries(generators) match {
         case t: TimeSeries[Boolean] => t
      }

      AndTimeSeries(first, second)
   }

   override def toString() = "AndGenerator(" + name + "," + a + "," + b + ")"

   override def equals(o: Any) = o match {
      case that: AndGenerator => that.name == this.name &&
         that.a == this.a &&
         that.b == this.b
      case _ => false
   }
}

class OrGenerator(name: Option[String],
                   val a: Either[String, Generator[Any]],
                   val b: Either[String, Generator[Any]]) extends Generator[Any](name, "or")
{
   override def timeseries(generators: (String) => Generator[Any]) =
   {
      val first = Model.generator(generators)(a).timeseries(generators) match {
         case t: TimeSeries[Boolean] => t
      }

      val second = Model.generator(generators)(b).timeseries(generators) match {
         case t: TimeSeries[Boolean] => t
      }

      OrTimeSeries(first, second)
   }

   override def toString() = "OrGenerator(" + name + "," + a + "," + b + ")"

   override def equals(o: Any) = o match {
      case that: OrGenerator => that.name == this.name &&
         that.a == this.a &&
         that.b == this.b
      case _ => false
   }
}

class NotGenerator(name: Option[String],
                  val generator: Either[String, Generator[Any]]) extends Generator[Any](name, "or")
{
   override def timeseries(generators: (String) => Generator[Any]) =
   {
      val base = Model.generator(generators)(generator).timeseries(generators) match {
         case t: TimeSeries[Boolean] => t
      }

      NotTimeSeries(base)
   }

   override def toString() = "Notenerator(" + name + "," + generator + ")"

   override def equals(o: Any) = o match {
      case that: NotGenerator => that.name == this.name &&
         that.generator == this.generator
      case _ => false
   }
}

class XorGenerator(name: Option[String],
                  val a: Either[String, Generator[Any]],
                  val b: Either[String, Generator[Any]]) extends Generator[Any](name, "xor")
{
   override def timeseries(generators: (String) => Generator[Any]) =
   {
      val first = Model.generator(generators)(a).timeseries(generators) match {
         case t: TimeSeries[Boolean] => t
      }

      val second = Model.generator(generators)(b).timeseries(generators) match {
         case t: TimeSeries[Boolean] => t
      }

      XorTimeSeries(first, second)
   }

   override def toString() = "XorGenerator(" + name + "," + a + "," + b + ")"

   override def equals(o: Any) = o match {
      case that: XorGenerator => that.name == this.name &&
         that.a == this.a &&
         that.b == this.b
      case _ => false
   }
}

class LimitedGenerator(name: Option[String],
                       val generator: Either[String, Generator[Any]],
                       val from: Option[LocalDateTime],
                       val to: Option[LocalDateTime]) extends Generator[Any](name, "limited")
{
   override def timeseries(generators: (String) => Generator[Any]) =
      LimitedTimeSeries(Model.generator(generators)(generator).timeseries(generators), from, to)

   override def toString() = "LimitedGenerator(" + name + "," + generator + "," + from + "," + to + ")"

   override def equals(o: Any) = o match {
      case that: LimitedGenerator => that.name == this.name &&
         that.generator == this.generator &&
         that.from == this.from &&
         that.to == this.to
      case _ => false
   }
}

class PartialGenerator(name: Option[String],
                       val generator: Either[String, Generator[Any]],
                       val from: Option[LocalDateTime],
                       val to: Option[LocalDateTime],
                       val missingRate: Option[Double]) extends Generator[Any](name, "partial")
{
   override def timeseries(generators: (String) => Generator[Any]) =
   {
      val ts = Model.generator(generators)(generator).timeseries(generators)
      PartialTimeSeries(ts, from, to, missingRate)
   }

   override def toString() = "PartialGenerator(" + name + "," + generator + "," + from + "," + to + "," + missingRate + ")"

   override def equals(o: Any) = o match {
      case that: PartialGenerator => that.name == this.name &&
         that.generator == this.generator &&
         that.from == this.from &&
         that.to == this.to &&
         that.missingRate == this.missingRate
      case _ => false
   }
}

class TimeShiftGenerator(name: Option[String],
                         val generator: Either[String, Generator[Any]],
                         val shift: Duration) extends Generator[Any](name, "time-shift")
{
   override def timeseries(generators: (String) => Generator[Any]) =
   {
      val ts = Model.generator(generators)(generator).timeseries(generators)
      TimeShiftTimeSeries(ts, shift)
   }

   override def toString() = "TimeShiftGenerator(" + name + "," + shift.getMillis + ")"

   override def equals(o: Any) = o match {
      case that: TimeShiftGenerator => that.name == this.name && that.shift == this.shift
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

   object ConditionalFormat extends RootJsonFormat[ConditionalGenerator]
   {
      def write(obj: ConditionalGenerator) =
      {
         val condition = (obj.condition match {
            case Left(s) => s.toJson
            case Right(g) => GeneratorFormat.write(g)
         }).toJson

         val success = (obj.success match {
            case Left(s) => s.toJson
            case Right(g) => GeneratorFormat.write(g)
         }).toJson

         var t = Map(
            "type" -> obj.`type`.toJson,
            "condition" -> condition,
            "success" -> success
         )

         if(obj.failure.isDefined)
         {
            val failure = (obj.failure.get match {
               case Left(s) => s.toJson
               case Right(g) => GeneratorFormat.write(g)
            }).toJson

            t = t.updated("failure", failure)
         }

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

         val condition = fields("condition") match {
            case JsString(s) => Left(s)
            case g => Right(GeneratorFormat.read(g))
         }

         val success = fields("success") match {
            case JsString(s) => Left(s)
            case g => Right(GeneratorFormat.read(g))
         }

         val failure = if(fields.contains("failure")) fields("failure") match {
                           case JsString(s) => Some(Left(s))
                           case g => Some(Right(GeneratorFormat.read(g)))
                        }
                         else None

         new ConditionalGenerator(name, condition, success, failure)
      }
   }

   object TrueFormat extends RootJsonFormat[TrueGenerator]
   {
      def write(obj: TrueGenerator) =
      {
         val t = Map(
            "type" -> obj.`type`.toJson
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

         new TrueGenerator(name)
      }
   }

   object FalseFormat extends RootJsonFormat[FalseGenerator]
   {
      def write(obj: FalseGenerator) =
      {
         val t = Map(
            "type" -> obj.`type`.toJson
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

         new FalseGenerator(name)
      }
   }

   object TransitionFormat extends RootJsonFormat[TransitionGenerator]
   {
      def write(obj: TransitionGenerator) =
      {
         val first = (obj.first match {
            case Left(s) => s.toJson
            case Right(g) => GeneratorFormat.write(g)
         }).toJson
         val second = (obj.second match {
            case Left(s) => s.toJson
            case Right(g) => GeneratorFormat.write(g)
         }).toJson

         val time = obj.time.toJson

         var t = Map(
            "type" -> obj.`type`.toJson,
            "first" -> first,
            "second" -> second,
            "time" -> time
         )

         if(obj.interval.isDefined)
            t = t.updated("interval" , obj.interval.get.toJson)

         if(obj.name.isDefined)
            t = t.updated("name" , obj.name.get.toJson)


         new JsObject(t)
      }

      def read(value: JsValue) =
      {
         val fields = value.asJsObject.fields

         val name = fields.get("name") .map(f => f match {
            case JsString(x) => x
         })

         val first = fields("first") match {
            case JsString(s) => Left(s)
            case g => Right(GeneratorFormat.read(g))
         }
         val second = fields("second") match {
            case JsString(s) => Left(s)
            case g => Right(GeneratorFormat.read(g))
         }

         val time = fields("time").convertTo[LocalDateTime]

         val duration = fields.get("duration").map(_.convertTo[Duration])
         val transition = fields.get("transition").map(_.convertTo[String])

         new TransitionGenerator(name, first, second, time, duration, transition)
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
         })

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
         val missingRate = fields.get("missing-rate").map(_.convertTo[Double])

         new LimitedGenerator(name, generator, from, to)
      }
   }

   object PartialFormat extends RootJsonFormat[PartialGenerator]
   {
      def write(obj: PartialGenerator) =
      {
         val name = obj.name
         val generator = (obj.generator match {
            case Left(s) => s.toJson
            case Right(g) => GeneratorFormat.write(g)
         }).toJson
         val from = obj.from.toJson
         val to = obj.to.toJson
         val missingRate = obj.missingRate


         var t = Map(
            "type" -> obj.`type`.toJson,
            "generator" -> generator,
            "from" -> from,
            "to" -> to
         )

         if(missingRate.isDefined) t = t.updated("missing-rate" , missingRate.toJson)
         if(name.isDefined) t = t.updated("name", name.toJson)

         new JsObject(t)
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

         new PartialGenerator(name, generator, from, to, missingRate)
      }
   }

   implicit object SeriesFormat extends RootJsonFormat[Series[Any]]
   {
      def write(obj: Series[Any]) =
      {
         val generator = (obj.generator match {
            case Left(s) => s.toJson
            case Right(g) => GeneratorFormat.write(g)
         })
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

   implicit object TimeShiftFormat extends RootJsonFormat[TimeShiftGenerator]
   {
      def write(obj: TimeShiftGenerator) =
      {
         val generator = (obj.generator match {
            case Left(s) => s.toJson
            case Right(g) => GeneratorFormat.write(g)
         })

         val name = obj.name

         var t = Map(
            "generator" -> generator,
            "shift" -> DurationFormat.write(obj.shift)
         )

         if(name.isDefined) t = t.updated("name", name.toJson)

         new JsObject(t)
      }

      def read(value: JsValue) =
      {
         val fields = value.asJsObject.fields

         val name = fields.get("name").map(_.convertTo[String])

         val generator = fields("generator") match {
            case JsString(s) => Left(s)
            case g => Right(GeneratorFormat.read(g))
         }

         val shift = fields("shift").convertTo[Duration]

         new TimeShiftGenerator(name, generator, shift)
      }
   }

   implicit object ThresholdFormat extends RootJsonFormat[ThresholdGenerator]
   {
      def write(obj: ThresholdGenerator) =
      {
         val generator = (obj.generator match {
            case Left(s) => s.toJson
            case Right(g) => GeneratorFormat.write(g)
         })

         val threshold = obj.threshold.toJson
         val included = obj.included.toJson

         val name = obj.name

         var t = Map(
            "generator" -> generator,
            "threshold" -> threshold,
            "included" -> included
         )

         if(name.isDefined) t = t.updated("name", name.toJson)

         new JsObject(t)
      }

      def read(value: JsValue) =
      {
         val fields = value.asJsObject.fields

         val name = fields.get("name").map(_.convertTo[String])

         val generator = fields("generator") match {
            case JsString(s) => Left(s)
            case g => Right(GeneratorFormat.read(g))
         }

         val threshold = fields("threshold").convertTo[Double]
         val included = fields.get("included").map(_.convertTo[Boolean])

         new ThresholdGenerator(name, generator, threshold, included)
      }
   }

   implicit object AndFormat extends RootJsonFormat[AndGenerator]
   {
      def write(obj: AndGenerator) =
      {
         val a = (obj.a match {
            case Left(s) => s.toJson
            case Right(g) => GeneratorFormat.write(g)
         })

         val b = (obj.b match {
            case Left(s) => s.toJson
            case Right(g) => GeneratorFormat.write(g)
         })

         val name = obj.name

         var t = Map(
            "a" -> a,
            "b" -> b
         )

         if(name.isDefined) t = t.updated("name", name.toJson)

         new JsObject(t)
      }

      def read(value: JsValue) =
      {
         val fields = value.asJsObject.fields

         val name = fields.get("name").map(_.convertTo[String])

         val a = fields("a") match {
            case JsString(s) => Left(s)
            case g => Right(GeneratorFormat.read(g))
         }

         val b = fields("b") match {
            case JsString(s) => Left(s)
            case g => Right(GeneratorFormat.read(g))
         }

         new AndGenerator(name, a, b)
      }
   }

   implicit object OrFormat extends RootJsonFormat[OrGenerator]
   {
      def write(obj: OrGenerator) =
      {
         val a = (obj.a match {
            case Left(s) => s.toJson
            case Right(g) => GeneratorFormat.write(g)
         })

         val b = (obj.b match {
            case Left(s) => s.toJson
            case Right(g) => GeneratorFormat.write(g)
         })

         val name = obj.name

         var t = Map(
            "a" -> a,
            "b" -> b
         )

         if(name.isDefined) t = t.updated("name", name.toJson)

         new JsObject(t)
      }

      def read(value: JsValue) =
      {
         val fields = value.asJsObject.fields

         val name = fields.get("name").map(_.convertTo[String])

         val a = fields("a") match {
            case JsString(s) => Left(s)
            case g => Right(GeneratorFormat.read(g))
         }

         val b = fields("b") match {
            case JsString(s) => Left(s)
            case g => Right(GeneratorFormat.read(g))
         }

         new OrGenerator(name, a, b)
      }
   }

   implicit object NotFormat extends RootJsonFormat[NotGenerator]
   {
      def write(obj: NotGenerator) =
      {
         val generator = (obj.generator match {
            case Left(s) => s.toJson
            case Right(g) => GeneratorFormat.write(g)
         })

         val name = obj.name

         var t = Map(
            "generator" -> generator
         )

         if(name.isDefined) t = t.updated("name", name.toJson)

         new JsObject(t)
      }

      def read(value: JsValue) =
      {
         val fields = value.asJsObject.fields

         val name = fields.get("name").map(_.convertTo[String])

         val generator = fields("generator") match {
            case JsString(s) => Left(s)
            case g => Right(GeneratorFormat.read(g))
         }

         new NotGenerator(name, generator)
      }
   }

   implicit object XorFormat extends RootJsonFormat[XorGenerator]
   {
      def write(obj: XorGenerator) =
      {
         val a = (obj.a match {
            case Left(s) => s.toJson
            case Right(g) => GeneratorFormat.write(g)
         })

         val b = (obj.b match {
            case Left(s) => s.toJson
            case Right(g) => GeneratorFormat.write(g)
         })

         val name = obj.name

         var t = Map(
            "a" -> a,
            "b" -> b
         )

         if(name.isDefined) t = t.updated("name", name.toJson)

         new JsObject(t)
      }

      def read(value: JsValue) =
      {
         val fields = value.asJsObject.fields

         val name = fields.get("name").map(_.convertTo[String])

         val a = fields("a") match {
            case JsString(s) => Left(s)
            case g => Right(GeneratorFormat.read(g))
         }

         val b = fields("b") match {
            case JsString(s) => Left(s)
            case g => Right(GeneratorFormat.read(g))
         }

         new XorGenerator(name, a, b)
      }
   }

   implicit val generatorFormat = GeneratorFormat
   implicit val armaModelFormat = jsonFormat5(ARMAModel)

   implicit val functionFormat = lazyFormat(FunctionFormat)
   implicit val aggregateFormat = lazyFormat(AggregateFormat)
   implicit val correlatedFormat = lazyFormat(CorrelatedFormat)
   implicit val logisticFormat = lazyFormat(LogisticFormat)
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
            case JsString("conditional") => ConditionalFormat.read(known)
            case JsString("true") => TrueFormat.read(known)
            case JsString("false") => FalseFormat.read(known)
            case JsString("transition") => transitionFormat.read(known)
            case JsString("limited") => limitedFormat.read(known)
            case JsString("partial") => partialFormat.read(known)
            case JsString("time-shift") => TimeShiftFormat.read(known)
            case JsString("threshold") => ThresholdFormat.read(known)
            case JsString("and") => AndFormat.read(known)
            case JsString("or") => OrFormat.read(known)
            case JsString("not") => OrFormat.read(known)
            case JsString("xor") => XorFormat.read(known)
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
      case x: ConditionalGenerator => ConditionalFormat.write(x)
      case x: TrueGenerator => TrueFormat.write(x)
      case x: FalseGenerator => FalseFormat.write(x)
      case x: TransitionGenerator => transitionFormat.write(x)
      case x: LimitedGenerator => limitedFormat.write(x)
      case x: PartialGenerator => partialFormat.write(x)
      case x: TimeShiftGenerator => TimeShiftFormat.write(x)
      case x: ThresholdGenerator => ThresholdFormat.write(x)
      case x: AndGenerator => AndFormat.write(x)
      case x: OrGenerator => OrFormat.write(x)
      case x: NotGenerator => NotFormat.write(x)
      case x: XorGenerator => XorFormat.write(x)
      case unrecognized => serializationError(s"Serialization problem ${unrecognized}")
   }
}