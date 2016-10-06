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

import be.cetic.rtsgen.generators.{Generator, TimeToJson}
import com.github.nscala_time.time.Imports._
import org.joda.time.Duration
import spray.json.{JsObject, JsString, JsValue, _}

case class Series[T](name: String, generator: Either[String, Generator[Any]], frequency: Duration) extends TimeToJson
{
   def toJson: JsValue = {
      val _generator = generator match
      {
         case Left(s) => s.toJson
         case Right(g) => g.toJson
      }

      new JsObject(Map(
         "name" -> name.toJson,
         "generator" -> _generator,
         "frequency" -> frequency.toJson
      ))
   }
}

object Series extends TimeToJson
{
   def apply[T](value: JsValue): Series[T] = {
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
