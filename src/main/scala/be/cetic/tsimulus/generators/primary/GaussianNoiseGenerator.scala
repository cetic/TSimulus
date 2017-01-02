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
import be.cetic.tsimulus.timeseries.primary.GaussianNoiseTimeSeries
import com.github.nscala_time.time.Imports._
import org.joda.time.Duration
import spray.json.{DefaultJsonProtocol, JsObject, JsString, JsValue}
import spray.json._


import scala.util.Random

/**
  * A generator for [[be.cetic.tsimulus.timeseries.primary.GaussianNoiseTimeSeries]].
  *
  * @param name The generator name.
  * @param seed The seed of the random number generator
  * @param std The standard deviation of the Gaussian distribution to follow.
  */
class GaussianNoiseGenerator(name: Option[String],
                    val seed: Int,
                    val std: Double) extends Generator[Double](name, "gaussian")
{
   override def timeseries(generators: String => Generator[Any]) = GaussianNoiseTimeSeries(seed, std)

   override def toString = "GaussianNoise(" + seed + ", " + std + ")"

   override def equals(o: Any) = o match {
      case that: GaussianNoiseGenerator => that.name == this.name &&
         that.seed == this.seed &&
         Math.abs(that.std - this.std) < 0.0001
      case _ => false
   }
   override def toJson: JsValue = {

      val t = Map(
         "type" -> `type`.toJson,
         "seed" -> seed.toJson,
         "std" -> std.toJson
      )

      new JsObject(
         name.map(n => t + ("name" -> n.toJson)).getOrElse(t)
      )
   }
}

object GaussianNoiseGenerator extends DefaultJsonProtocol with TimeToJson
{
   def apply(json: JsValue): GaussianNoiseGenerator = {

      val fields = json.asJsObject.fields
      val name = fields.get("name") .map(f => f match {
         case JsString(x) => x
      })

      val seed = fields("seed").convertTo[Int]
      val std = fields("std").convertTo[Double]

      new GaussianNoiseGenerator(name, seed, std)
   }
}