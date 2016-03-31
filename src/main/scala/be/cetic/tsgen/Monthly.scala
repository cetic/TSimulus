package be.cetic.tsgen

import org.apache.commons.math3.analysis.interpolation.AkimaSplineInterpolator
import org.joda.time._

/**
  * Represents cyclic variation of a time series on a month basis.
  *
  * @param controlPoints The value a time series must pass by at a given time.
  */
case class Monthly(controlPoints: Map[Int, Double]) extends CyclicTimeSeries
{
   private val month_value = Map[Int, Int](
      DateTimeConstants.JANUARY -> 0,
      DateTimeConstants.FEBRUARY -> 1,
      DateTimeConstants.MARCH -> 2,
      DateTimeConstants.APRIL -> 3,
      DateTimeConstants.MAY -> 4,
      DateTimeConstants.JUNE -> 5,
      DateTimeConstants.JULY -> 6,
      DateTimeConstants.AUGUST -> 7,
      DateTimeConstants.SEPTEMBER -> 8,
      DateTimeConstants.OCTOBER -> 9,
      DateTimeConstants.NOVEMBER -> 10,
      DateTimeConstants.DECEMBER -> 11)

   /**
     * @param month A month in a particular year.
     * @return The datetime that define the limit of the month.
     */
   private def month_threshold(month : YearMonth): LocalDateTime =
   {
      val begining = (month.toLocalDate(1).toLocalDateTime(new LocalTime(0,0,0)))
      val end = (begining plus Months.ONE minus Days.ONE) withTime(23,59,49,999)

      val period = new Period(begining, end)
      val half_duration = period.toStandardDuration dividedBy 2

      val threshold = begining plus half_duration

      return threshold
   }

   val interpolator = {

      val entries = controlPoints.map { case (key, value) => (month_value(key).toDouble, value)}.toSeq.sortBy(entry => entry._1)

      val tempo_date = entries.map(_._1)
      val before_date = - (11 - tempo_date.last + 1)
      val after_date = tempo_date.head + 12
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
   def compute(time: LocalDateTime): Double = {


      val current_year_month = new YearMonth(time.getYear, time.getMonthOfYear)

      val active_year_month = if(time isBefore month_threshold(current_year_month)) current_year_month minusMonths 1
                              else current_year_month

      val next_year_month = active_year_month plusMonths 1

      val max_duration = Seconds.secondsBetween(month_threshold(active_year_month), month_threshold(next_year_month))
      val current_duration = Seconds.secondsBetween(month_threshold(active_year_month), time)

      val ratio = current_duration.getSeconds.toDouble / max_duration.getSeconds

      return interpolator.value((active_year_month.getMonthOfYear - 1) + ratio)
   }
}
