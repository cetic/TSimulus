package be.cetic.tsgen.core.timeseries.primary

import be.cetic.tsgen.core.timeseries.IndependantTimeSeries
import com.github.nscala_time.time.Imports._
import org.apache.commons.math3.analysis.interpolation.AkimaSplineInterpolator
import org.joda.time.Seconds

/**
  * Represents cyclic variation of a time series on a monthly basis.
  *
  * @param controlPoints The value a time series must pass by at a given time.
  */
case class MonthlyTimeSeries(controlPoints: Map[Int, Double]) extends IndependantTimeSeries[Double]
{
   /**
     * @param month A month in a particular year.
     * @return The datetime that define the limit of the month.
     */
   private def month_threshold(month : YearMonth): LocalDateTime =
   {
      val begining = (month.toLocalDate(1).toLocalDateTime(new LocalTime(0,0,0)))
      val end = (begining + 1.month - 1.day) withTime(23,59,49,999)

      val duration = new Duration(begining.toDateTime(DateTimeZone.UTC), end.toDateTime(DateTimeZone.UTC))
      val half_duration = duration / 2

      return begining + half_duration
   }

   val interpolator = {

      val entries = controlPoints.map { case (key, value) => ((key-1).toDouble, value)}.toSeq.sortBy(entry => entry._1)

      val tempo_date = entries.map(_._1)
      val before_date = - (11 - tempo_date.last + 1)
      val after_date = tempo_date.head + 12
      val dates = (before_date +: tempo_date :+ after_date).toArray

      val tempo_values = entries.map(_._2)
      val values = (tempo_values.last +: tempo_values :+ tempo_values.head).toArray

      new AkimaSplineInterpolator().interpolate(dates, values)
   }

   def compute(time: LocalDateTime): Option[Double] = {


      val current_year_month = new YearMonth(time.getYear, time.getMonthOfYear)

      val active_year_month = if(time < month_threshold(current_year_month)) current_year_month minus 1.month
                              else current_year_month

      val next_year_month = active_year_month plus 1.month

      val max_duration = Seconds.secondsBetween(month_threshold(active_year_month), month_threshold(next_year_month))
      val current_duration = Seconds.secondsBetween(month_threshold(active_year_month), time)

      val ratio = current_duration.getSeconds.toDouble / max_duration.getSeconds

      return Some(interpolator.value((active_year_month.getMonthOfYear - 1) + ratio))
   }
}
