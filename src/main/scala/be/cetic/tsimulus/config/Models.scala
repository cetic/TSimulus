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

package be.cetic.tsimulus.config

import be.cetic.tsimulus.generators._
import be.cetic.tsimulus.generators.binary._
import be.cetic.tsimulus.generators.composite._
import be.cetic.tsimulus.generators.missing.{DefaultGenerator, LimitedGenerator, PartialGenerator, UndefinedGenerator}
import be.cetic.tsimulus.generators.primary._
import be.cetic.tsimulus.generators.primary.dt._
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


case class ARMAModel(phi: Option[Seq[Double]],
                     theta: Option[Seq[Double]],
                     std: Double,
                     c: Double,
                     seed: Option[Long])


object GeneratorFormat extends JsonFormat[Generator[Any]]
{

   def deserializationError(s: String): Generator[Any] = throw DeserializationException(s)

   def serializationError(s: String): JsValue = throw new SerializationException(s)

   override def read(json: JsValue): Generator[Any] = json match {
      case known:JsObject if known.fields.contains("type") =>
         known.fields("type") match{
            case JsString("arma") => ARMAGenerator(known)
            case JsString("daily") => DailyGenerator(known)
            case JsString("weekly") => WeeklyGenerator(known)
            case JsString("monthly") => MonthlyGenerator(known)
            case JsString("yearly") => YearlyGenerator(known)
            case JsString("constant") => ConstantGenerator(known)
            case JsString("aggregate") => AggregateGenerator(known)
            case JsString("divide") => DivideGenerator(known)
            case JsString("correlated") => CorrelatedGenerator(known)
            case JsString("logistic") => LogisticGenerator(known)
            case JsString("conditional") => ConditionalGenerator(known)
            case JsString("true") => TrueGenerator(known)
            case JsString("false") => FalseGenerator(known)
            case JsString("transition") => TransitionGenerator(known)
            case JsString("binary-transition") => BinaryTransitionGenerator(known)
            case JsString("window") => SlidingWindowGenerator(known)
            case JsString("limited") => LimitedGenerator(known)
            case JsString("partial") => PartialGenerator(known)
            case JsString("time-shift") => TimeShiftGenerator(known)
            case JsString("function") => FunctionGenerator(known)
            case JsString("and") => AndGenerator(known)
            case JsString("or") => OrGenerator(known)
            case JsString("not") => NotGenerator(known)
            case JsString("xor") => XorGenerator(known)
            case JsString("implies") => ImpliesGenerator(known)
            case JsString("equiv") => EquivGenerator(known)
            case JsString("undefined") => UndefinedGenerator(known)
            case JsString("first-of") => DefaultGenerator(known)
            case JsString("greater-than") => GreaterThanGenerator(known)
            case JsString("lesser-than") => LesserThanGenerator(known)
            case JsString("gaussian") => GaussianNoiseGenerator(known)
            case JsString("year") => YearGenerator(known)
            case JsString("month") => MonthGenerator(known)
            case JsString("dom") => DayOfMonthGenerator(known)
            case JsString("hour") => HourGenerator(known)
            case JsString("minute") => MinuteGenerator(known)
            case JsString("second") => SecondTimeGenerator(known)
            case JsString("ms") => MillisecondTimeGenerator(known)
            case JsString("week") => WeekGenerator(known)
            case JsString("dow") => DayOfWeekGenerator(known)
            case unknown => deserializationError(s"unknown Generator object: $unknown")
         }
      case unknown => deserializationError(s"unknown  Generator object: $unknown")
   }

   override def write(obj: Generator[Any]): JsValue = obj.toJson
}