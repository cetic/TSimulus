package be.cetic.tsgen

import org.joda.time.LocalDateTime

/**
  * A time series being function of an other time series.
  *
  * @param generator the time series on which this time series is based
  * @param f the function to apply to the generator to get this time series.
  */
case class RelativeTimeSeries[A,T](generator: TimeSeries[A], f: A => T) extends TimeSeries[T]
{
   override def compute(times: Stream[LocalDateTime]) = generator .compute(times) map(f)
}
