package be.cetic.tsgen

import be.cetic.tsgen.config.Configuration
import com.github.nscala_time.time.Imports._
import org.joda.time.DateTimeConstants
import spray.json._
import DefaultJsonProtocol._
import be.cetic.tsgen.config.GeneratorLeafFormat._
import be.cetic.tsgen.timeseries._


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
           |         "name": "daily-generator",
           |         "type": "daily",
           |         "points": {"10:00:00.000": 12, "17:00:00.000": 15, "20:00:00.000": 11, "02:00:00.000": 5, "04:00:00.000": 7}
           |      },
           |      {
           |         "name": "weekly-generator",
           |         "type": "weekly",
           |         "points": {"monday": 12, "tuesday": 15, "friday": 12, "saturday": 5}
           |      },
           |      {
           |         "name": "yearly-generator",
           |         "type": "yearly",
           |         "points": {"2016": 12, "2017": 15}
           |      },
           |      {
           |         "name": "noisy-daily",
           |         "type": "aggregate",
           |         "aggregator": "sum",
           |         "generators": [
           |            "daily-generator",
           |            {
           |                "type": "arma",
           |                "model": { "std": 0.5, "c": 0, "seed": 159357},
           |                "timestep": 3600000
           |            }
           |         ]
           |      },
           |      {
           |         "name": "noise-generator",
           |         "type": "arma",
           |         "model": { "std": 0.75, "c": 0, "seed": 159357},
           |         "timestep": 3600000
           |      },
           |      {
           |         "name":  "partial-daily",
           |         "type": "partial",
           |         "generator": "daily-generator",
           |         "missing-rate": 0.8
           |      },
           |      {
           |         "name":  "limited-daily",
           |         "type": "limited",
           |         "generator": "daily-generator",
           |         "from": "2016-01-01 10:00:00.000"
           |      },
           |      {
           |         "name":  "transition-daily-noise-daily",
           |         "type": "transition",
           |         "first": {
           |            "type": "transition",
           |            "first": "daily-generator",
           |            "second": "noise-generator",
           |            "time": "2016-01-01 12:00:00.000",
           |            "duration": 7200000,
           |            "transition": "sigmoid"
           |         },
           |         "second": "daily-generator",
           |         "time": "2016-01-02 12:00:00.000",
           |         "duration": 7200000
           |      },
           |      {
           |         "name":  "logistic-daily",
           |         "type": "logistic",
           |         "generator": "daily-generator",
           |         "location": 13,
           |         "scale": 8
           |      },
           |      {
           |         "name": "transition-0-1-0",
           |         "type": "transition",
           |         "first": { "type": "constant", "value":0 },
           |         "second": {
           |            "type": "transition",
           |            "first": {"type": "constant", "value":1 },
           |            "second": {"type": "constant", "value":0 },
           |            "time": "2016-01-01 07:30:00.000",
           |            "duration": 4000000,
           |            "transition": "sigmoid"
           |         },
           |         "time": "2016-01-01 03:30:00.000",
           |         "duration": 4000000,
           |         "transition": "sigmoid"
           |      }
           |   ],
           |   "series": [
           |      {
           |         "name": "series-A",
           |         "generator": "transition-0-1-0",
           |         "frequency": 60000
           |      }
           |   ],
           |   "from": "2016-01-01 00:00:00.000",
           |   "to": "2016-01-01 10:00:00.000"
           |}
         """.stripMargin.parseJson

      val config = document.convertTo[Configuration]

      println("date;series;value")

      generate(config2TimeSeries(config)) foreach (e => println(dtf.print(e._1) + ";" + e._2 + ";" + e._3))
   }

   private def config2TimeSeries(config: Configuration): Map[String, Stream[(LocalDateTime, Any)]] =
   {
      val ts = config.timeSeries

      ts.map(series =>
      {
         val name = series._1
         val frequency = series._2._2
         val values = series._2._1
         val times = Main.sampling(config.from, config.to, frequency)

         (name -> values.compute(times).filter(e => e._2.isDefined).map(e => (e._1, e._2.get)))
      })
   }

   private def generate(series: Map[String, Stream[(LocalDateTime, Any)]]): Stream[(LocalDateTime, String, Any)] =
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
