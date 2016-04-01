package be.cetic.tsgen

import org.joda.time.LocalDateTime

/**
  * A time series generator able to provide each value of the time series independently.
  */
trait IndependantTimeSeries[T] extends TimeSeries[T]
{
   /**
     * @param time A point in the time series
     * @return the value associated to the given time in the time series.
     */
   def compute(time: LocalDateTime): T

   def compute(times: Stream[LocalDateTime]): Stream[T] = times.map(compute)
}
