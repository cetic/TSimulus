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

package be.cetic.rtsgen.generators.composite

import be.cetic.rtsgen.config.Model
import be.cetic.rtsgen.generators.Generator
import be.cetic.rtsgen.timeseries.TimeSeries
import be.cetic.rtsgen.timeseries.composite.TransitionTimeSeries
import com.github.nscala_time.time.Imports._
import org.joda.time.LocalDateTime

/**
  * A generator for [[be.cetic.rtsgen.timeseries.composite.TransitionTimeSeries]].
  */
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
         case Some(s) =>
            s match {
               case "linear" => linear
               case "sigmoid" => sigmoid
               case "exp" => superlin
               case _ => linear
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

   override def toString = "TransitionGenerator(" + name + "," + first + "," + second + "," + time + "," + interval + "," + f + ")"

   override def equals(o: Any) = o match {
      case that: TransitionGenerator => that.name == this.name &&
         that.first == this.first &&
         that.second == this.second &&
         that.time == this.time &&
         that.interval == this.interval
      case _ => false
   }
}
