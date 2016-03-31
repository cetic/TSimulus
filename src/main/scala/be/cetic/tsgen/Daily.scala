package be.cetic.tsgen

import java.time.LocalDate

import org.apache.commons.math3.analysis.interpolation.{AkimaSplineInterpolator, SplineInterpolator}
import org.joda.time.{LocalDateTime, LocalTime}

/**
  * Represents cyclic variation of a time series on a daily basis.
  * @param controlPoints The value a time series must pass by at a given time.
  */
case class Daily(val controlPoints : Map[LocalTime, Double]) extends CyclicTimeSeries
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

      println(times.mkString(","))
      println(values.mkString(","))

      new AkimaSplineInterpolator().interpolate(times, values)
   }

   /**
     * Determines the value associated to a date time
     * @param time the moment for which a value must be provided
     * @return the value associated to time.
     */
   def compute(time: LocalDateTime): Double = interpolator.value(time.getMillisOfDay)
}
