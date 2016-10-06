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
import com.github.nscala_time.time.Imports._
import org.joda.time.{Duration, LocalDateTime, LocalTime}
import spray.json.{DefaultJsonProtocol, JsArray, JsNumber, JsObject, JsString, JsValue, RootJsonFormat, _}

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
         case unrecognized => serializationError(s"Serialization problem $unrecognized")
      }
   }

   implicit object LocalTimeJsonFormat extends RootJsonFormat[LocalTime] {
      def write(t: LocalTime) = JsString(ttf.print(t))
      def read(value: JsValue) = value match {
         case JsString(s) => ttf.parseLocalTime(s)
         case unknown => deserializationError(s"unknown LocalTime object: $unknown")
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
         val name = json.asJsObject.fields.get("name").map
         {
            case JsString(x) => x
         }

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
         val name = json.asJsObject.fields.get("name").map
         {
            case JsString(x) => x
         }

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
         val name = fields.get("name").map
         {
            case JsString(x) => x
         }

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

         val name = json.asJsObject.fields.get("name").map
         {
            case JsString(x) => x
         }

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

         val name = fields.get("name").map
         {
            case JsString(x) => x
         }

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

         val name = fields.get("name").map
         {
            case JsString(x) => x
         }

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
         val generators = obj.generators.map
         {
            case Left(s) => s.toJson
            case Right(x) => GeneratorFormat.write(x)
         }.toJson

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

         val name = fields.get("name").map
         {
            case JsString(x) => x
         }

         val aggregator = fields("aggregator").convertTo[String]
         val generators = fields("generators") match {
            case JsArray(x) => x.map
            {
               case JsString(s) => Left(s)
               case g => Right(GeneratorFormat.read(g))
            }.toList
         }

         new AggregateGenerator(name, aggregator, generators)
      }
   }

   object DivideFormat extends RootJsonFormat[DivideGenerator] {
      def write(obj: DivideGenerator) =
      {
         val name = obj.name.toJson
         val numerator = (obj.numerator match {
            case Left(s) => s.toJson
            case Right(x) => GeneratorFormat.write(x)
         }).toJson

         val denominator = (obj.denominator match {
            case Left(s) => s.toJson
            case Right(x) => GeneratorFormat.write(x)
         }).toJson

         val t = Map(
            "type" -> obj.`type`.toJson,
            "numerator" -> numerator,
            "denominator" -> denominator
         )

         new JsObject(
            obj.name.map(n => t + ("name" -> n.toJson)).getOrElse(t)
         )
      }

      def read(value: JsValue) =
      {
         val fields = value.asJsObject.fields

         val name = fields.get("name").map
         {
            case JsString(x) => x
         }

         val numerator = fields("numerator") match {
            case JsString(s) => Left(s)
            case g => Right(GeneratorFormat.read(g))
         }

         val denominator = fields("denominator") match {
            case JsString(s) => Left(s)
            case g => Right(GeneratorFormat.read(g))
         }

         new DivideGenerator(name, numerator, denominator)
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

         val name = fields.get("name").map
         {
            case JsString(x) => x
         }

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

         val name = fields.get("name").map
         {
            case JsString(x) => x
         }

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

         val name = fields.get("name").map
         {
            case JsString(x) => x
         }

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

         val name = fields.get("name").map
         {
            case JsString(x) => x
         }

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

         val name = fields.get("name").map
         {
            case JsString(x) => x
         }

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
            "time" -> time,
            "transition" -> obj.f.getOrElse("linear").toJson
         )

         if(obj.interval.isDefined)
            t = t.updated("duration", obj.interval.get.toJson)

         if(obj.name.isDefined)
            t = t.updated("name", obj.name.get.toJson)

         new JsObject(t)
      }

      def read(value: JsValue) =
      {
         val fields = value.asJsObject.fields

         val name = fields.get("name").map
         {
            case JsString(x) => x
         }

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

   object SlidingWindowFormat extends RootJsonFormat[SlidingWindowGenerator]
   {
      def write(obj: SlidingWindowGenerator) =
      {
         val generator = (obj.generator match {
            case Left(s) => s.toJson
            case Right(g) => GeneratorFormat.write(g)
         }).toJson

         var t = Map(
            "window-length" -> obj.duration.toJson
         )

         if(obj.name.isDefined)
            t = t.updated("name" , obj.name.get.toJson)

         new JsObject(t)
      }

      def read(value: JsValue) =
      {
         val fields = value.asJsObject.fields

         val name = fields.get("name").map
         {
            case JsString(x) => x
         }

         val generator = fields("generator") match {
            case JsString(s) => Left(s)
            case g => Right(GeneratorFormat.read(g))
         }

         val aggregator = fields("aggregator") match { case JsString(x) => x }
         val duration = fields("window-length").convertTo[Duration]

         new SlidingWindowGenerator(name, aggregator, generator, duration)
      }
   }

   object LimitedFormat extends RootJsonFormat[LimitedGenerator]
   {
      def write(obj: LimitedGenerator) =
      {
         val name = obj.name.toJson
         val generator = obj.generator match
         {
            case Left(s) => s.toJson
            case Right(g) => GeneratorFormat.write(g)
         }

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

         val name = fields.get("name").map
         {
            case JsString(x) => x
         }

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

         val name = fields.get("name").map
         {
            case JsString(x) => x
         }

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
         val generator = obj.generator match
         {
            case Left(s) => s.toJson
            case Right(g) => GeneratorFormat.write(g)
         }
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
            "generators" -> obj.generators.map(g => g.map(GeneratorFormat.write)).toJson,
            "exported" -> obj.series.toJson,
            "from" -> obj.from.toJson,
            "to" -> obj.to.toJson
         ))
      }

      def read(value: JsValue) =
      {
         val fields = value.asJsObject.fields

         val generators = fields.get("generators").map
         {
            case JsArray(l) => l.map(GeneratorFormat.read)
            case _ => throw new ClassCastException
         }

         val series = fields("exported") match {
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
         val generator = obj.generator match
         {
            case Left(s) => s.toJson
            case Right(g) => GeneratorFormat.write(g)
         }

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
         val generator = obj.generator match
         {
            case Left(s) => s.toJson
            case Right(g) => GeneratorFormat.write(g)
         }

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
         val a = obj.a match
         {
            case Left(s) => s.toJson
            case Right(g) => GeneratorFormat.write(g)
         }

         val b = obj.b match
         {
            case Left(s) => s.toJson
            case Right(g) => GeneratorFormat.write(g)
         }

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
         val a = obj.a match
         {
            case Left(s) => s.toJson
            case Right(g) => GeneratorFormat.write(g)
         }

         val b = obj.b match
         {
            case Left(s) => s.toJson
            case Right(g) => GeneratorFormat.write(g)
         }

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
         val generator = obj.generator match
         {
            case Left(s) => s.toJson
            case Right(g) => GeneratorFormat.write(g)
         }

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
         val a = obj.a match
         {
            case Left(s) => s.toJson
            case Right(g) => GeneratorFormat.write(g)
         }

         val b = obj.b match
         {
            case Left(s) => s.toJson
            case Right(g) => GeneratorFormat.write(g)
         }

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

   object UndefinedFormat extends RootJsonFormat[UndefinedGenerator]
   {
      def write(obj: UndefinedGenerator) =
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

         val name = fields.get("name").map
         {
            case JsString(x) => x
         }

         new UndefinedGenerator(name)
      }
   }

   object DefaultFormat extends RootJsonFormat[DefaultGenerator]
   {
      def write(obj: DefaultGenerator) =
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

         val name = fields.get("name").map
         {
            case JsString(x) => x
         }

         val generators = fields("generators") match {
            case JsArray(l) => l.map
            {
               case JsString(s) => Left(s)
               case g => Right(GeneratorFormat.read(g))
            }
         }

         new DefaultGenerator(name, generators)
      }
   }

   implicit val generatorFormat = GeneratorFormat
   implicit val armaModelFormat = jsonFormat5(ARMAModel)

   implicit val functionFormat = lazyFormat(FunctionFormat)
   implicit val aggregateFormat = lazyFormat(AggregateFormat)
   implicit val divideFormat = lazyFormat(DivideFormat)
   implicit val correlatedFormat = lazyFormat(CorrelatedFormat)
   implicit val logisticFormat = lazyFormat(LogisticFormat)
   implicit val transitionFormat = lazyFormat(TransitionFormat)
   implicit val limitedFormat = lazyFormat(LimitedFormat)
   implicit val partialFormat = lazyFormat(PartialFormat)
   implicit val conditionalFormat = lazyFormat(ConditionalFormat)
}
