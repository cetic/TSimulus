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

package be.cetic.rtsgen.generators

import com.github.nscala_time.time.Imports._
import org.joda.time.{Duration, LocalDateTime, LocalTime}
import spray.json.{JsString, JsValue, RootJsonFormat, _}


trait TimeToJson extends DefaultJsonProtocol
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

   implicit object DurationFormat extends RootJsonFormat[Duration] {
      def write(d: Duration) = d.getMillis.toJson
      def read(value: JsValue) = new Duration(value.toString.toLong)
   }

   def either2json(element: Either[String,Generator[Any]]) = element match {
      case Left(s) => s.toJson
      case Right(g) => g.toJson
   }
}
