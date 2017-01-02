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

import be.cetic.tsimulus.generators.{Generator, TimeToJson}
import be.cetic.tsimulus.timeseries.TimeSeries
import com.github.nscala_time.time.Imports._
import org.joda.time.LocalDateTime
import spray.json.{JsArray, JsObject, JsValue, _}

case class Configuration(generators: Option[Seq[Generator[Any]]],
                         series: Seq[Series[Any]],
                         from: LocalDateTime,
                         to: LocalDateTime) extends TimeToJson
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

   def firstOrderGenerators: Map[String, Generator[Any]] =
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

   def toJson: JsValue = {
      new JsObject(Map(
         "generators" -> generators.map(g => g.map(_.toJson)).toJson,
         "exported" -> series.map(s => s.toJson).toJson,
         "from" -> from.toJson,
         "to" -> to.toJson
      ))
   }
}

object Configuration extends TimeToJson
{
   def apply(value: JsValue): Configuration = {
      val fields = value.asJsObject.fields

      val generators = fields.get("generators").map
      {
         case JsArray(l) => l.map(GeneratorFormat.read)
         case _ => throw new ClassCastException
      }

      val series = fields("exported") match {
         case JsArray(x) => x.map(Series[Any](_)).toSeq
         case _ => throw new ClassCastException
      }

      val from = fields("from").convertTo[LocalDateTime]
      val to = fields("to").convertTo[LocalDateTime]

      Configuration(generators, series, from, to)
   }
}
