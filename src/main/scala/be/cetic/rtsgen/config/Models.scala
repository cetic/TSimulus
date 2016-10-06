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

package be.cetic.rtsgen.config

import be.cetic.rtsgen.generators._
import be.cetic.rtsgen.generators.binary._
import be.cetic.rtsgen.generators.composite._
import be.cetic.rtsgen.generators.missing.{DefaultGenerator, LimitedGenerator, PartialGenerator, UndefinedGenerator}
import be.cetic.rtsgen.generators.primary._
import be.cetic.rtsgen.timeseries._
import com.github.nscala_time.time.Imports._
import org.joda.time.LocalDateTime
import spray.json.{JsString, _}

object Model
{
   def generator(generators: String => Generator[Any])(element: Either[String, Generator[Any]]): Generator[Any] =
      element match {
         case Left(s) => generators(s)
         case Right(g) => g
      }
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



case class ARMAModel(phi: Option[Seq[Double]],
                     theta: Option[Seq[Double]],
                     std: Double,
                     c: Double,
                     seed: Option[Long])


object GeneratorFormat extends JsonFormat[Generator[Any]]
{
   import GeneratorLeafFormat._

   def deserializationError(s: String): Generator[Any] = throw DeserializationException(s)


   def serializationError(s: String): JsValue = throw new SerializationException(s)

   override def read(json: JsValue): Generator[Any] = json match {
      case known:JsObject if known.fields.contains("type") =>
         known.fields("type") match{
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
            case unknown => deserializationError(s"unknown Generator object: $unknown")
         }
      case unknown => deserializationError(s"unknown  Generator object: $unknown")
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
      case unrecognized => serializationError(s"Serialization problem $unrecognized")
   }
}