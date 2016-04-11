package be.cetic.tsgen

import com.github.nscala_time.time.Imports._
import org.joda.time.DateTimeConstants


object Main
{
   /**
     * Creates a sequence of times, from a start time to a given limit.
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

      val daily = DailyTimeSeries(Map(
         new LocalTime(2, 0) -> 2D,
         new LocalTime(14, 0) -> 10D,
         new LocalTime(17, 0) -> 7D
      ))

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

      val yearly = YearlyTimeSeries(Map(
         2017 -> 5,
         2018 -> 17,
         2019 -> 21,
         2020 -> 17,
         2021 -> 5
      ))

      val noise = RandomWalkTimeSeries(ARMA(std=0.5, theta = Array(1)), timeStep = Duration.standardHours(1))


      val times: Stream[LocalDateTime] = sampling( new LocalDateTime(2016,1,1,0,0,0),
                                                   new LocalDateTime(2016,12,31,23,59,59),
                                                   Duration.standardHours(1))

      val gen = DailyTimeSeries(Map(
         new LocalTime(11, 0, 0) -> 6,
         new LocalTime(13, 0, 0) -> 8,
         new LocalTime(17, 0, 0) -> 13,
         new LocalTime(21, 0, 0) -> 3))


   }
}
