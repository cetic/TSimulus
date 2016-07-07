package be.cetic.tsgen.core.timeseries.composite

import be.cetic.tsgen.core.timeseries.TimeSeries
import org.joda.time.LocalDateTime

/**
  * A time series in which each value is defined as the function of the corresponding value
  * of an other time series.
  *
  * @param generator the underlying generator
  * @param f the function to apply to defined values
  */
case class FunctionTimeSeries[T](generator: TimeSeries[T], f: (LocalDateTime, T) => Option[T]) extends TimeSeries[T]
{
  override def compute(times: Stream[LocalDateTime]) =
       generator.compute(times)
                .map {case (t,v) => (t, v.map(f(t,_)).flatten)}
}
