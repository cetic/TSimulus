package be.cetic.tsgen

import org.apache.commons.math3.analysis.interpolation.AkimaSplineInterpolator
import org.joda.time._

import scala.annotation.tailrec

/**
  * Represents cyclic variation of a time series on a yearly basis.
  * Since years are on an open scale, a smooth interpolation of values is not waranted.
  *
  * @param controlPoints The value a time series must pass by at a given time.
  */
case class YearlyTimeSeries(controlPoints: Map[Int, Double]) extends IndependantTimeSeriesGenerator[Double]
{
   private val beginning = controlPoints.keys.min
   private val end = controlPoints.keys.max
   private val length = new Period(year_threshold(beginning), year_threshold(end))

   /**
     * @param year A year.
     * @return The datetime that define the limit of the year.
     */
   private def year_threshold(year : Int): LocalDateTime =
   {
      val begining = new LocalDateTime(year, 1, 1, 0, 0, 0)
      val end = new LocalDateTime(year, 12, 31, 23, 59, 59)

      val duration = new Duration(begining.toDateTime(DateTimeZone.UTC), end.toDateTime(DateTimeZone.UTC))
      val half_duration = duration dividedBy 2

      return begining plus half_duration
   }

   val interpolator =
   {

      val entries = controlPoints.map { case (key, value) => ((key-1).toDouble, value)}.toSeq.sortBy(entry => entry._1)

      val tempo_date = entries.map(_._1)
      val before_date = tempo_date.head - 1
      val after_date = tempo_date.last + 1
      val dates = (before_date +: tempo_date :+ after_date).toArray

      val tempo_values = entries.map(_._2)
      val values = (tempo_values.last +: tempo_values :+ tempo_values.head).toArray

      new AkimaSplineInterpolator().interpolate(dates, values)
   }

   private def correctedTime(time: LocalDateTime): LocalDateTime =
   {
      if (time isBefore year_threshold(beginning-1)) return correctedTime(time plus length)
      if (time isAfter year_threshold(end)) return correctedTime(time minus length)
      return time
   }

   def compute(time: LocalDateTime): Double =
   {
      val current_time = correctedTime(time)
      val current_year = current_time.getYear

      val active_year = if(current_time isBefore year_threshold(current_year)) current_year - 1
                        else current_year

      val next_year = active_year + 1

      val max_duration = Seconds.secondsBetween(year_threshold(active_year), year_threshold(next_year))
      val current_duration = Seconds.secondsBetween(year_threshold(active_year), current_time)

      val ratio = current_duration.getSeconds.toDouble / max_duration.getSeconds

      return interpolator.value(active_year + ratio)
   }
}
