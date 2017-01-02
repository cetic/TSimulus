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

package be.cetic.tsimulus.timeseries.primary

import be.cetic.tsimulus.timeseries.TimeSeries
import com.github.nscala_time.time.Imports._
import org.joda.time.{DateTimeZone, Duration, LocalDateTime}

import scala.annotation.tailrec

/**
  * A time series based on an ARMA model.
  *
  * The ARMA model provides a series of discrete values. In order to bind them to a particular date time,
  * a linear regression is used.
  *
  * @param arma the ARMA model used to generate a time series based on a random walk.
  * @param origin the moment at which the random walk starts.
  * @param timeStep the duration between two consecutive steps.
  */
case class RandomWalkTimeSeries(arma: ARMA, origin: LocalDateTime, timeStep: Duration) extends TimeSeries[Double]
{
   override def compute(times: Stream[LocalDateTime]): Stream[(LocalDateTime, Option[Double])] =
   {
      if(times.isEmpty) Stream()
      else {

         val first_time = times.head

         val value_back = arma.reverse_series
         val moment_back = computeReverseTimes(origin)
         val data_back = (moment_back zip value_back).drop(1) // The first datum is already in the forward stream
                                                     .takeWhile(x => x._1 >= first_time)
                                                     .reverse

         val data_forward = computeTimes(origin) zip arma.series
         val data = data_back ++ data_forward

         def process(blocks: Stream[((LocalDateTime, Double), (LocalDateTime, Double))], dates: Stream[LocalDateTime]): Stream[(LocalDateTime, Option[Double])] =
         {
            val block_head = blocks.head

            dates match {
               case Stream() => Stream.empty
               case head #:: tail => {
                  if(head > block_head._2._1) process(blocks.tail, dates)
                  else{
                     val tStart = block_head._1._1
                     val tEnd = block_head._2._1

                     val vStart = block_head._1._2
                     val vEnd = block_head._2._2

                     val result = Some(interpolation(tStart, vStart, tEnd, vEnd, head))

                     (head, result) #:: process(blocks, tail)
                  }
               }
            }


         }

         process(data zip data.drop(1), times)
      }
   }

   private def computeTimes(time: LocalDateTime): Stream[LocalDateTime] = time #:: computeTimes(time + timeStep)
   private def computeReverseTimes(time: LocalDateTime): Stream[LocalDateTime] = time #:: computeReverseTimes(time - timeStep)

   private def intervals[T](xs: Stream[T]) = xs zip xs.tail

   override def compute(time: LocalDateTime): Option[Double] =
   {
      val values = if(time >= origin) arma.series
                   else arma.reverse_series

      val moments = if(time >= origin) computeTimes(origin)
                    else computeReverseTimes(origin)

      val valueIntervals = intervals(values)
      val momentIntervals = intervals(moments)
      val data = momentIntervals zip valueIntervals

      val block = data.dropWhile(d => !((d._1._1 >= time && d._1._2 <= time) || (d._1._1 <= time && d._1._2 >= time)))
                      .head

      val tStart = block._1._1
      val tEnd = block._1._2

      val vStart = block._2._1
      val vEnd = block._2._2

      Some(interpolation(tStart, vStart, tEnd, vEnd, time))
   }

   private def interpolation(
                               x1: LocalDateTime,
                               v1: Double,
                               x2: LocalDateTime,
                               v2: Double,
                               time: LocalDateTime
                            ): Double =
   {
      if(x1 > x2) interpolation(x2, v2, x1, v1, time)
      else{
         val deltaTime = new Duration(x1.toDateTime(DateTimeZone.UTC), x2.toDateTime(DateTimeZone.UTC)).getMillis.toDouble
         val deltaValue = v2 - v1

         val ratio = new Duration(x1.toDateTime(DateTimeZone.UTC), time.toDateTime(DateTimeZone.UTC)).getMillis / deltaTime
         v1 + (deltaValue * ratio)
      }
   }
}
