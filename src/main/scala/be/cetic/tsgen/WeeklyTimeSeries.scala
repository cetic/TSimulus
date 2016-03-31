package be.cetic.tsgen

import org.apache.commons.math3.analysis.interpolation.AkimaSplineInterpolator
import org.joda.time._

/**
  * Represents cyclic variation of a time series on a weekly basis.
  *
  * @param controlPoints The value a time series must pass by at a given time.
  */
case class WeeklyTimeSeries(controlPoints: Map[Int, Double]) extends TimeSeriesGenerator
{
   /**
     * @param day A day.
     * @return The datetime that define the limit of the day.
     */
   private def day_threshold(day : LocalDate): LocalDateTime =
   {
      val begining = day.toLocalDateTime(new LocalTime(0,0,0))
      val end = day.toLocalDateTime(new LocalTime(23, 59, 59))

      val duration = new Duration(begining.toDateTime(DateTimeZone.UTC), end.toDateTime(DateTimeZone.UTC))
      val half_duration = duration dividedBy 2

      return begining plus half_duration
   }

   val interpolator =
   {

      val entries = controlPoints.map { case (key, value) => ((key-1).toDouble, value)}.toSeq.sortBy(entry => entry._1)

      val tempo_date = entries.map(_._1)
      val before_date = - (6 - tempo_date.last + 1)
      val after_date = tempo_date.head + 7
      val dates = (before_date +: tempo_date :+ after_date).toArray

      val tempo_values = entries.map(_._2)
      val values = (tempo_values.last +: tempo_values :+ tempo_values.head).toArray

      new AkimaSplineInterpolator().interpolate(dates, values)
   }

   /**
     * Determines the value associated to a date time
     * @param time the moment for which a value must be provided
     * @return the value associated to time.
     */
   def compute(time: LocalDateTime): Double =
   {

      val current_day = time.toLocalDate

      val active_day =  if(time isBefore day_threshold(current_day)) current_day minusDays 1
                        else current_day

      val next_day = active_day plusDays 1

      val max_duration = Seconds.secondsBetween(day_threshold(active_day),day_threshold(next_day))
      val current_duration = Seconds.secondsBetween(day_threshold(active_day), time)

      val ratio = current_duration.getSeconds.toDouble / max_duration.getSeconds

      return interpolator.value((active_day.getDayOfWeek - 1) + ratio)
   }
}
