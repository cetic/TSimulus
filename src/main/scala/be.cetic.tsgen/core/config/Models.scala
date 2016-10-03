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

package be.cetic.tsgen.core.config

import be.cetic.tsgen.core._
import be.cetic.tsgen.core.config.GeneratorLeafFormat.ConstantFormat
import be.cetic.tsgen.core.timeseries._
import be.cetic.tsgen.core.timeseries.binary._
import be.cetic.tsgen.core.timeseries.composite._
import be.cetic.tsgen.core.timeseries.missing.{DefaultTimeSeries, LimitedTimeSeries, PartialTimeSeries, UndefinedTimeSeries}
import be.cetic.tsgen.core.timeseries.primary._
import com.github.nscala_time.time.Imports._
import org.apache.commons.math3.stat.StatUtils
import org.joda.time.{DateTimeConstants, LocalDateTime, LocalTime}
import spray.json.{JsString, _}

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
      val agg = aggregationFunction(aggregator)

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

class DivideGenerator(name: Option[String],
                      val numerator: Either[String, Generator[Any]],
                      val denominator: Either[String, Generator[Any]]) extends Generator[Double](name, "divide")
{
   override def timeseries(gen: String => Generator[Any]) =
   {
      val num = Model.generator(gen)(numerator).timeseries(gen) match {
         case t: TimeSeries[Double] => t
      }

      val den = Model.generator(gen)(denominator).timeseries(gen) match {
         case t: TimeSeries[Double] => t
      }


      new DivideTimeSeries(num, den)
   }

   override def toString() = "DivideGenerator(" + name + "," + numerator + "," + denominator + ")"

   override def equals(o: Any) = o match {
      case that: DivideGenerator => that.name == this.name && that.numerator == this.numerator && that.denominator == this.denominator
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

   override def toString() = "TransitionGenerator(" + name + "," + first + "," + second + "," + time + "," + interval + "," + f + ")"

   override def equals(o: Any) = o match {
      case that: TransitionGenerator => that.name == this.name &&
         that.first == this.first &&
         that.second == this.second &&
         that.time == this.time &&
         that.interval == this.interval
      case _ => false
   }
}

class SlidingWindowGenerator(name: Option[String],
                             val aggregator: String,
                             val generator: Either[String, Generator[Any]],
                             val duration: Duration) extends Generator[Double](name, "window")
{
   override def timeseries(generators: (String) => Generator[Any]) = {

      val aggregation = { x: Seq[(Duration, Double)] => aggregator match {
         case "sum" => x.map(_._2).sum
         case "product" => x.map(_._2).sum
         case "min" => x.map(_._2).min
         case "max" => x.map(_._2).max
         case "mean" => x.map(_._2).sum / x.size
         case "median" => StatUtils.percentile(x.map(_._2).toArray, 50)
         case _ => x.map(_._2).sum / x.size
      }}

      val d = new Duration(duration)

      val base = Model.generator(generators)(generator).timeseries(generators) match {
         case t: TimeSeries[Double] => t
      }

      SlidingWindowTimeSeries[Double](base, duration, aggregation)
   }

   override def toString() = "SlidingWindowGenerator(" + name + "," + aggregator + "," + generator + "," + duration + ")"

   override def equals(o: Any) = o match {
      case that: SlidingWindowGenerator => that.name == this.name &&
         that.aggregator == this.aggregator &&
         that.generator == this.generator &&
         that.duration == this.duration
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

class UndefinedGenerator(name: Option[String]) extends Generator[Any](name, "undefined")
{
   override def timeseries(generators: (String) => Generator[Any]) = new UndefinedTimeSeries()

   override def toString() = "UndefinedGenerator(" + name + ")"

   override def equals(o: Any) = o match {
      case that: UndefinedGenerator => that.name == this.name
      case _ => false
   }
}

class DefaultGenerator(name: Option[String], val gens: Seq[Either[String, Generator[Any]]]) extends Generator[Any](name, "first-of")
{
   override def timeseries(generators: (String) => Generator[Any]) =
   {
      val underlyings = gens.map(g => Model.generator(generators)(g).timeseries(generators) match {
         case t: TimeSeries[Any] => t
      })

      DefaultTimeSeries(underlyings)
   }

   override def toString() = "UndefinedGenerator(" + name + "," + gens + ")"

   override def equals(o: Any) = o match {
      case that: DefaultGenerator => that.gens == this.gens
      case _ => false
   }
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
            case JsString("divide") => divideFormat.read(known)
            case JsString("correlated") => correlatedFormat.read(known)
            case JsString("logistic") => logisticFormat.read(known)
            case JsString("conditional") => ConditionalFormat.read(known)
            case JsString("true") => TrueFormat.read(known)
            case JsString("false") => FalseFormat.read(known)
            case JsString("transition") => transitionFormat.read(known)
            case JsString("window") => SlidingWindowFormat.read(known)
            case JsString("limited") => limitedFormat.read(known)
            case JsString("partial") => partialFormat.read(known)
            case JsString("time-shift") => TimeShiftFormat.read(known)
            case JsString("threshold") => ThresholdFormat.read(known)
            case JsString("and") => AndFormat.read(known)
            case JsString("or") => OrFormat.read(known)
            case JsString("not") => OrFormat.read(known)
            case JsString("xor") => XorFormat.read(known)
            case JsString("undefined") => UndefinedFormat.read(known)
            case JsString("first-of") => DefaultFormat.read(known)
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
      case x: DivideGenerator => divideFormat.write(x)
      case x: CorrelatedGenerator => correlatedFormat.write(x)
      case x: LogisticGenerator => logisticFormat.write(x)
      case x: ConditionalGenerator => ConditionalFormat.write(x)
      case x: TrueGenerator => TrueFormat.write(x)
      case x: FalseGenerator => FalseFormat.write(x)
      case x: TransitionGenerator => transitionFormat.write(x)
      case x: SlidingWindowGenerator => SlidingWindowFormat.write(x)
      case x: LimitedGenerator => limitedFormat.write(x)
      case x: PartialGenerator => partialFormat.write(x)
      case x: TimeShiftGenerator => TimeShiftFormat.write(x)
      case x: ThresholdGenerator => ThresholdFormat.write(x)
      case x: AndGenerator => AndFormat.write(x)
      case x: OrGenerator => OrFormat.write(x)
      case x: NotGenerator => NotFormat.write(x)
      case x: XorGenerator => XorFormat.write(x)
      case x: UndefinedGenerator => UndefinedFormat.write(x)
      case x: DefaultGenerator => DefaultFormat.write(x)
      case unrecognized => serializationError(s"Serialization problem ${unrecognized}")
   }
}