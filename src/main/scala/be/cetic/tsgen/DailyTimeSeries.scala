package be.cetic.tsgen

import org.apache.commons.math3.analysis.interpolation.{AkimaSplineInterpolator}
import org.joda.time.{LocalDateTime, LocalTime}

/**
  * Represents cyclic variation of a time series on a daily basis.
  * @param controlPoints The value a time series must pass by at a given time.
  */
case class DailyTimeSeries(val controlPoints : Map[LocalTime, Double]) extends IndependantTimeSeries[Double]
{
   val interpolator = {

      val entries = controlPoints.toSeq.sortBy(entry => entry._1.getMillisOfDay)

      val times = {
         val tempo = entries.map(_._1.getMillisOfDay.toDouble)
         val millisInADay = 24*60*60*1000
         (-(millisInADay - tempo.last) +: tempo :+ (millisInADay + tempo.head)).toArray
      }

      val values = {
         val tempo = entries.map(_._2)
         (tempo.last +: tempo :+ tempo.head).toArray
      }

      new AkimaSplineInterpolator().interpolate(times, values)
   }

   def compute(time: LocalDateTime): Double = interpolator.value(time.getMillisOfDay)
}
