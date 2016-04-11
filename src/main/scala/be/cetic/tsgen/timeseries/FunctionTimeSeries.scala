package be.cetic.tsgen.timeseries

import org.joda.time.LocalDateTime

/**
  * A time series in which each value is defined as the function of the corresponding value
  * of an other time series.
  */
case class FunctionTimeSeries[T](generator: TimeSeries[T], f: T => Option[T]) extends TimeSeries[T]
{
  override def compute(times: Stream[LocalDateTime]) =
       generator.compute(times)
                .map {case (t,v) => (t, v.map(f).flatten)}
}
