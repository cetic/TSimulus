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

package be.cetic.tsgen.core.timeseries.primary

import be.cetic.tsgen.core.timeseries.TimeSeries
import com.github.nscala_time.time.Imports._
import org.joda.time.{DateTimeZone, Duration, LocalDateTime}

/**
  * A time series based on an ARMA model.
  *
  * The ARMA model provides a series of discrete values. In order to bind them to a particular date time,
  * a linear regression is used.
  *
  * @param arma the ARMA model used to generate a time series based on a random walk.
  * @param timeStep the duration between two consecutive steps.
  */
case class RandomWalkTimeSeries(arma: ARMA, timeStep: Duration) extends TimeSeries[Double]
{
   override def compute(times: Stream[LocalDateTime]) =
   {
      val deltaT = timeStep.getMillis.toDouble

      val timeInterval = intervals(computeTimes(times.head))
      val valueInterval = intervals(arma.series)

      val data = timeInterval zip valueInterval

      def process(times: Stream[LocalDateTime],
                  data: Stream[((LocalDateTime, LocalDateTime) , (Double, Double))]) : Stream[(LocalDateTime, Some[Double])] = times match {
         case Stream.Empty => Stream.empty
         case time #:: timeRest =>
         {
            val time = times.head
            val dataRest = data.dropWhile({ case ((timeStart: LocalDateTime, timeEnd: LocalDateTime),
            (valStart: Double, valEnd: Double)) => time > timeEnd
            })

            val tStart = dataRest.head._1._1
            val tEnd = dataRest.head._1._2

            val vStart = dataRest.head._2._1
            val vEnd = dataRest.head._2._2

            val timeRatio = new Duration(tStart.toDateTime(DateTimeZone.UTC),
                                         time.toDateTime(DateTimeZone.UTC)).getMillis / deltaT

            val value = vStart + (vEnd - vStart) * timeRatio

            (time, Some(value)) #:: process(timeRest, dataRest)
         }
      }

      process(times, data)
   }

   private def computeTimes(time: LocalDateTime): Stream[LocalDateTime] = time #:: computeTimes(time + timeStep)

   private def intervals[T](xs: Stream[T]) = xs zip xs.tail
}
