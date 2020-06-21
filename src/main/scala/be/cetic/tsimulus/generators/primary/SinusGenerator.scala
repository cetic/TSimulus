/*
 * Copyright Cetic ASBL
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

package be.cetic.tsimulus.generators.primary

import be.cetic.tsimulus.generators.{Generator, TimeToJson}
import be.cetic.tsimulus.timeseries.TimeSeries
import be.cetic.tsimulus.timeseries.primary.SinusTimeSeries
import org.joda.time.LocalDateTime
import spray.json._


/**
  * A generator for [[be.cetic.tsimulus.timeseries.primary.SinusTimeSeries]].
  */
class SinusGenerator(name: Option[String],
                      val origin: LocalDateTime,
                      val period: Long) extends Generator[Double](name, "sinus")
{
   override def toString = s"SinusGenerator(${name}, ${origin}, ${period})"

   /**
     * @param generators an entity able to discover all the first-level generators on the
     *                   basis of their name.
     * @return the time series associated to this generator
     */
   override def timeseries(generators: String => Generator[Any]): TimeSeries[Double] =
      SinusTimeSeries(origin, period)

   override def equals(o: Any) = o match {
      case that: SinusGenerator => that.name == this.name
      case _ => false
   }

   override def toJson: JsValue =
   {
      val t = Map(
         "type" -> `type`.toJson,
         "origin" -> origin.toJson,
         "period" -> period.toJson
      )

      new JsObject(
         name.map(n => t + ("name" -> n.toJson)).getOrElse(t)
      )
   }
}

object SinusGenerator extends DefaultJsonProtocol with TimeToJson
{
   def apply(name: Option[String], origin: LocalDateTime, period: Long) = new SinusGenerator(name, origin, period)
   def apply(name: String, origin: LocalDateTime, period: Long) = new SinusGenerator(Some(name), origin, period)
   def apply(origin: LocalDateTime, period: Long) = new SinusGenerator(None, origin, period)

   def apply(json: JsValue): SinusGenerator =
   {
      val fields = json.asJsObject.fields

      val name = fields.get("name") .map(f => f match {
         case JsString(x) => x
      })

      val origin: LocalDateTime = fields.get("origin").get.convertTo[LocalDateTime]
      val period: Long = fields.get("period").get.convertTo[Long]

      new SinusGenerator(name, origin, period)
   }
}

