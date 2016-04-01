package be.cetic.tsgen
import org.joda.time.{Duration, LocalDateTime}

import com.github.nscala_time.time.Imports._

/**
  * Temporally filters the effects of a time series.
  *
  * @param generator: The filtered time series.
  * @param from: The time before which the filtered time series has no effect (its filtered values are 0).
  *              If None, the filtered time series has no lower limit.
  * @param to     The time after which the filtered time series has no effect (its filtered values are 0).
  *               If None, the filtered time series has no upper limit.
  *               Must be greated than from (if both are defined).
  * @param fadein The duration between the lower limit and the time at which the filtered time series is fully effective.
  *               Between the lower limit of the filter and the end of the fadein, the value of the time series
  *               linearly increases to reach its nominal value at the end of the fadein.
  * @param fadeout The duration between the time at which the filtered time series becomes less effective and its upper limit.
  *                Between the begining of the fadeout and the upper limit, the value of the time series linearly
  *                decreases to reach 0.
  **/
case class TimeSeriesFilter(
                              generator: TimeSeries[Double],
                              from: Option[LocalDateTime],
                              to: Option[LocalDateTime],
                              fadein: Duration = Duration.ZERO,
                              fadeout: Duration = Duration.ZERO
                           ) extends TimeSeries[Double]
{
   override def compute(times: Stream[LocalDateTime]): Stream[Double] =
   {
      (times zip generator.compute(times)).map(e => math.min(fadeInFilter(e._1, e._2), fadeOutFilter(e._1, e._2)))
   }

   private def fadeInFilter(time: LocalDateTime, value: Double): Double = from match {
      case None => value
      case Some(start) => {
         if(time < start) 0
         else
         {
            if(time > start + fadein) value
            else{
               // real fade-in
               val ratio = new Duration(start.toDateTime(DateTimeZone.UTC), time.toDateTime(DateTimeZone.UTC)).getMillis / fadein.getMillis.toDouble
               ratio * value
            }
         }
      }
   }

   private def fadeOutFilter(time: LocalDateTime, value: Double): Double = to match {
      case None => value
      case Some(end) => {
         if(time > end) 0
         else
         {
            if(time < end - fadeout) value
            else{
               // real fade-out
               val ratio = 1 - new Duration((end - fadeout).toDateTime(DateTimeZone.UTC), time.toDateTime(DateTimeZone.UTC)).getMillis / fadeout.getMillis.toDouble
               ratio * value
            }
         }
      }
   }
}
