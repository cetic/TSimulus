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

package be.cetic.tsgen.core.config.core

import be.cetic.tsgen.core.config.core.config.Configuration
import com.github.nscala_time.time.Imports._
import org.joda.time.DateTimeConstants
import spray.json._
import DefaultJsonProtocol._
import be.cetic.tsgen.core.config.core.config.GeneratorLeafFormat._
import be.cetic.tsgen.core.config.core.timeseries._
import be.cetic.tsgen.core.config.core.timeseries.primary.{ARMA, MonthlyTimeSeries, RandomWalkTimeSeries, WeeklyTimeSeries}


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
      return sampling(start, end, new Duration(duration.getMillis / (nbTimes-1)))
   }


   def main(args: Array[String])
   {
      val model = ARMA(std = 0.1, c = 0)
      val dtf = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss")

      // model.series take 500 foreach (e => println(e.formatted("%2f")))

     val monthly = MonthlyTimeSeries(Map(
         DateTimeConstants.JANUARY -> -6.3,
         DateTimeConstants.FEBRUARY -> -6.9,
         DateTimeConstants.MARCH -> -2.7,
         DateTimeConstants.APRIL -> 0.1,
         DateTimeConstants.MAY -> 2.8,
         DateTimeConstants.JUNE -> 6.4,
         DateTimeConstants.JULY -> 8.8,
         DateTimeConstants.AUGUST -> 8.6,
         DateTimeConstants.SEPTEMBER -> 2.5,
         DateTimeConstants.OCTOBER -> -0.4,
         DateTimeConstants.NOVEMBER -> -0.9,
         DateTimeConstants.DECEMBER -> -1))

      val weekly = WeeklyTimeSeries(Map(
         DateTimeConstants.MONDAY -> 0,
         DateTimeConstants.TUESDAY -> 0.5,
         DateTimeConstants.WEDNESDAY -> 1,
         DateTimeConstants.THURSDAY -> 2,
         DateTimeConstants.FRIDAY -> 2.5,
         DateTimeConstants.SATURDAY -> 1.5,
         DateTimeConstants.SUNDAY -> 0.5
      ))

       val document =
         """
           |{
           |   "generators": [
           |      {
           |         "name": "normal-cst",
           |         "type": "constant",
           |         "value": 0.1
           |      },
           |      {
           |         "name": "noise",
           |         "type": "arma",
           |         "model": {
           |            "std": 0.05,
           |            "c": 0
           |         },
           |         "timestep": 30000
           |      },
           |      {
           |         "name": "normal",
           |         "type": "aggregate",
           |         "aggregator": "max",
           |         "generators": [{
           |            "type": "constant",
           |            "value": 0
           |         },
           |         {
           |            "type": "aggregate",
           |            "aggregator": "sum",
           |            "generators": ["normal-cst", "noise"]
           |         }]
           |      },
           |      {
           |         "name": "rush",
           |         "type": "aggregate",
           |         "aggregator": "max",
           |         "generators": [{
           |            "type": "constant",
           |            "value": 0
           |         },
           |         {
           |            "type": "aggregate",
           |            "aggregator": "sum",
           |            "generators": ["noise", {"type": "constant", "value": 4} ]
           |         }]
           |      },
           |      {
           |         "name": "actual",
           |         "type": "transition",
           |         "first": "normal",
           |         "second": {
           |            "type": "transition",
           |            "first": "rush",
           |            "second": "normal",
           |            "time": "2016-01-01 10:00:00.000",
           |            "duration": 1200000,
           |            "transition": "exp"
           |         },
           |         "time": "2016-01-01 02:00:00.000",
           |         "duration": 28800000,
           |         "transition": "exp"
           |      }
           |   ],
           |   "exported": [
           |      {
           |         "name": "actual",
           |         "generator": "actual",
           |         "frequency": 30000
           |      }
           |   ],
           |   "from": "2016-01-01 00:00:00.000",
           |   "to": "2016-01-02 00:00:00.000"
           |}
         """.stripMargin.parseJson

      val config = document.convertTo[Configuration]

      println("date;series;value")

      generate(config2Results(config)) foreach (e => println(dtf.print(e._1) + ";" + e._2 + ";" + e._3))


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

         (name -> values.compute(times).filter(e => e._2.isDefined).map(e => (e._1, e._2.get)))
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
