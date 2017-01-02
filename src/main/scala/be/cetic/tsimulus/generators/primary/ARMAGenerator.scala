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

package be.cetic.tsimulus.generators.primary

import be.cetic.tsimulus.config.ARMAModel
import be.cetic.tsimulus.generators.{Generator, TimeToJson}
import be.cetic.tsimulus.timeseries.primary.{ARMA, RandomWalkTimeSeries}
import com.github.nscala_time.time.Imports._
import org.joda.time.Duration
import spray.json._

import scala.util.Random

/**
  * A generator for [[be.cetic.tsimulus.timeseries.primary.RandomWalkTimeSeries]].
  *
  * @param name The generator name.
  * @param model The model expressing the constraints
  * @param origin The moment at which the ARMA steps start.
  * @param timestep The duration between two steps.
  */
class ARMAGenerator(name: Option[String],
                    val model: ARMAModel,
                    val origin: LocalDateTime,
                    val timestep: Duration) extends Generator[Double](name, "arma")
{
   implicit val armaModelFormat = jsonFormat5(ARMAModel)

   override def timeseries(generators: String => Generator[Any]) =
      RandomWalkTimeSeries(
         ARMA(
            model.phi.getOrElse(Seq()).toArray,
            model.theta.getOrElse(Seq()).toArray,
            model.std,
            model.c,
            model.seed.getOrElse(Random.nextLong())
         ),
         origin,
         timestep
      )

   override def toString = "ARMAGenerator(" + model + ", " + origin + ", " + timestep + ")"

   override def equals(o: Any) = o match {
      case that: ARMAGenerator => that.name == this.name &&
                                  that.model == this.model &&
                                  that.timestep == this.timestep &&
                                  that.origin == this.origin
      case _ => false
   }

   override def toJson: JsValue = {

      val t = Map(
         "type" -> `type`.toJson,
         "origin" -> origin.toJson,
         "model" -> model.toJson,
         "timestep" -> timestep.toJson
      )

      new JsObject(
         name.map(n => t + ("name" -> n.toJson)).getOrElse(t)
      )
   }
}

object ARMAGenerator extends DefaultJsonProtocol with TimeToJson
{
   implicit val armaModelFormat = jsonFormat5(ARMAModel)

   def apply(json: JsValue): ARMAGenerator = {

      val fields = json.asJsObject.fields
      val name = fields.get("name") .map(f => f match {
         case JsString(x) => x
      })

      val model = fields("model").convertTo[ARMAModel]
      val origin = fields("origin").convertTo[LocalDateTime]
      val timestep = fields("timestep").convertTo[Duration]

      new ARMAGenerator(name, model, origin, timestep)
   }
}
