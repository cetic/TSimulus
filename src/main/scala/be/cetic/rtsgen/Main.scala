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

package be.cetic.rtsgen

import be.cetic.rtsgen.config.{ARMAModel, Configuration}
import be.cetic.rtsgen.generators.primary.ARMAGenerator
import be.cetic.rtsgen.timeseries._
import be.cetic.rtsgen.timeseries.primary.{ARMA, MonthlyTimeSeries, RandomWalkTimeSeries, WeeklyTimeSeries}
import com.github.nscala_time.time.Imports._
import org.joda.time.DateTimeConstants
import spray.json._


object Main
{
   /**
     * Creates a sequence of times, from a start time to a given limit.
     *
     * @param start the start time.
     * @param end the end time, no retrieved time can be set after this time.
     * @param duration the time space between two consecutive times.
     * @return a sequence of regularly spaced times, starting by start.
     */
   def sampling(start: LocalDateTime,
                end: LocalDateTime,
                duration: Duration): Stream[LocalDateTime] = if(start isAfter end) Stream.empty
                                                             else start #:: sampling(start plus duration, end, duration)

   /**
     * Creates a sequence of times, from a start time to a given limit.
     *
     * @param start the start time.
     * @param end the end time, no retrieved time can be set after this time.
     * @param nbTimes the number of times that must be retrieved
     * @return a sequence of regularly spaced times, starting by start.
     */
   def sampling(start: LocalDateTime,
                end: LocalDateTime,
                nbTimes: Int): Stream[LocalDateTime] =
   {
      val duration = new Duration(start.toDateTime(DateTimeZone.UTC), end.toDateTime(DateTimeZone.UTC))
      sampling(start, end, new Duration(duration.getMillis / (nbTimes-1)))
   }


   def main(args: Array[String]): Unit =
   {
      val dates = sampling(new LocalDateTime(2016, 1, 2, 0, 0), new LocalDateTime(2016, 1, 3, 0, 0), 1 minute)
      val ts = new RandomWalkTimeSeries(ARMA(Array(), Array(), 0.01, 0, 42), new LocalDateTime(2016, 1, 2, 5, 0), 5 minutes)

      dates.foreach(d => {
         val v = ts.compute(d)
         println(d.toString + ";" + v.get)
      })


   }

   def config2Results(config: Configuration): Map[String, Stream[(LocalDateTime, Any)]] =
      timeSeries2Results(config.timeSeries, config.from, config.to)

   def timeSeries2Results(ts: Map[String, (TimeSeries[Any], _root_.com.github.nscala_time.time.Imports.Duration)],
                          from: LocalDateTime,
                          to: LocalDateTime): Map[String, Stream[(LocalDateTime, Any)]] =
   {
      ts.map(series =>
      {
         val name = series._1
         val frequency = series._2._2
         val values = series._2._1
         val times = Main.sampling(from, to, frequency)

         name -> values.compute(times).filter(e => e._2.isDefined).map(e => (e._1, e._2.get))
      })
   }

   def generate(series: Map[String, Stream[(LocalDateTime, Any)]]): Stream[(LocalDateTime, String, Any)] =
   {
      val cleanedMap = series.filterNot(_._2.isEmpty)

      if (cleanedMap.isEmpty) Stream.Empty
      else
      {
         val selected = cleanedMap.minBy(e => e._2.head._1)

         val head = selected._2.head
         val tail = selected._2.tail

         val next = series.updated(selected._1, tail)

         (head._1, selected._1, head._2) #:: generate(next)
      }
   }
}
