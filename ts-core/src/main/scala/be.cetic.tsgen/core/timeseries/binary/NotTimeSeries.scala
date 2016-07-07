package be.cetic.tsgen.core.timeseries.binary

import be.cetic.tsgen.core.timeseries.TimeSeries
import org.joda.time.LocalDateTime

/**
  * A binary time series which is the negation of an other time series.
  * This time series is true iff the base time series is false.
  *
  * If the value is not defined for the base time series, then the NOT value is not defined.
  */
case class NotTimeSeries(base: TimeSeries[Boolean]) extends TimeSeries[Boolean]
{
   override def compute(times: Stream[LocalDateTime]) = base.compute(times).map { case (t,v) => (t, v.map(!_)) }
}
