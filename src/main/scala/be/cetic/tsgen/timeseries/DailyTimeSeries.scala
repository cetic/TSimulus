package be.cetic.tsgen.timeseries

import org.apache.commons.math3.analysis.interpolation.AkimaSplineInterpolator
import org.joda.time.{LocalDateTime, LocalTime}

/**
  * Represents cyclic variation of a time series on a daily basis.
  * @param controlPoints The value a time series must pass by at a given time.
  */
case class DailyTimeSeries(val controlPoints : Map[LocalTime, Double]) extends IndependantTimeSeries[Double]
{
   val interpolator = {
      val millisInADay = 24*60*60*1000
      val entries = controlPoints.toSeq.sortBy(entry => entry._1.getMillisOfDay)

      val a = entries.map { case (t,v) => (t.getMillisOfDay, v)}

      val before = (-(millisInADay - a.last._1), a.last._2)
      val penultimate = (-(millisInADay - a.takeRight(2).head._1), a.takeRight(2).head._2)

      val after = (millisInADay + a.head._1, a.head._2)
      val afterAfter = (millisInADay + a(1)._1, a(1)._2)

      val modified = penultimate +: before +: a :+ after :+ afterAfter

      new AkimaSplineInterpolator().interpolate(modified.map(_._1.toDouble).toArray, modified.map(_._2).toArray)
   }

   def compute(time: LocalDateTime) = Some(interpolator.value(time.getMillisOfDay))
}
