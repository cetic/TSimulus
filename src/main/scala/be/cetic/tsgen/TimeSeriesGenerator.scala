package be.cetic.tsgen

import org.joda.time.LocalDateTime

/**
  * A cyclic time series
  */
trait TimeSeriesGenerator
{
   /**
     * @param times a series of time for which values must be computed.
     * @return the values associated to the specified times.
     */
   def compute(times: Stream[LocalDateTime]): Stream[Double]
}
