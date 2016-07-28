package be.cetic.tsgen.core.timeseries.composite

import be.cetic.tsgen.core.timeseries.TimeSeries
import org.joda.time.LocalDateTime

/**
  * This time series generator divides a time series by an other one.
  *
  * @param numerator the generator that represents the numerator.
  * @param denominator the generator that represents the denominator.
  */
class DivideTimeSeries(val numerator: TimeSeries[Double], val denominator: TimeSeries[Double]) extends TimeSeries[Double]
{
   override def compute(times: Stream[LocalDateTime]) =
   {
      val fractions = (numerator.compute(times) zip denominator.compute(times)).map( entry => entry._1._1 -> (entry._1._2, entry._2._2))

      fractions.map {
         case (time, (None, _)) => (time, None)
         case (time, (_, None)) => (time, None)
         case (time, (_, Some(0))) => (time, None)
         case (time, (Some(num), Some(den))) => (time, Some(num / den))
      }
   }

   override def toString = "DivideTimeSeries(" + numerator + "," + denominator + ")"
}