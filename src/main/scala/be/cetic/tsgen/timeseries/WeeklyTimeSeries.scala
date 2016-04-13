package be.cetic.tsgen.timeseries

import com.github.nscala_time.time.Imports._
import org.apache.commons.math3.analysis.interpolation.AkimaSplineInterpolator
import org.joda.time.Seconds

/**
  * Represents cyclic variation of a time series on a weekly basis.
  *
  * @param controlPoints The value a time series must pass by at a given time.
  */
case class WeeklyTimeSeries(controlPoints: Map[Int, Double]) extends IndependantTimeSeries[Double]
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
      val half_duration = duration / 2

      return begining + half_duration
   }

   val interpolator =
   {

      val entries = controlPoints.map { case (key, value) => ((key-1).toDouble, value)}.toSeq.sortBy(entry => entry._1)

      val tempo_date = entries.map(_._1)
      val before_date = - (7 - tempo_date.last)
      val penultimate_date = - (7 - tempo_date.takeRight(2).head)

      val after_date = tempo_date.head + 7
      val after_after_date = tempo_date(1) + 7

      val dates = (penultimate_date +: before_date +: tempo_date :+ after_date :+ after_after_date).toArray

      val tempo_values = entries.map(_._2)
      val values = (tempo_values.takeRight(2).head +: tempo_values.last +: tempo_values :+ tempo_values.head :+ tempo_values(2)).toArray

      new AkimaSplineInterpolator().interpolate(dates, values)
   }

   def compute(time: LocalDateTime): Option[Double] =
   {

      val current_day = time.toLocalDate

      val active_day =  if(time isBefore day_threshold(current_day)) current_day - 1.day
                        else current_day

      val next_day = active_day + 1.day

      val max_duration = Seconds.secondsBetween(day_threshold(active_day),day_threshold(next_day))
      val current_duration = Seconds.secondsBetween(day_threshold(active_day), time)

      val ratio = current_duration.getSeconds.toDouble / max_duration.getSeconds

      return Some(interpolator.value((active_day.getDayOfWeek - 1) + ratio))
   }
}
