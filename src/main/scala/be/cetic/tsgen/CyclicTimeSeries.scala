package be.cetic.tsgen

import org.joda.time.LocalDateTime

/**
  * A cyclic time series
  */
trait CyclicTimeSeries
{
   /**
     *
     * @param time A point in the time series
     * @return the value associated to the given time in the time series.
     */
   def compute(time: LocalDateTime): Double
}
