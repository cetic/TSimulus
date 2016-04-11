package be.cetic.tsgen

import org.joda.time.LocalDateTime

/**
  * A cyclic time series
  */
trait TimeSeries[+T]
{
   /**
     * @param times a series of time for which values must be computed. Each time must be greater than or equal to
     *              the previous one.
     * @return the values associated to the specified times, if specified.
     */
   def compute(times: Stream[LocalDateTime]): Stream[(LocalDateTime, Option[T])]
}
