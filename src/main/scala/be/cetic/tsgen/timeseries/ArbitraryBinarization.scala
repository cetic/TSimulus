package be.cetic.tsgen.timeseries
import org.joda.time.LocalDateTime

/**
  * A binary time series based on an arbitrary predicate. The returned values will be false if the
  * predicate is respected for the underlying values.
  */
case class ArbitraryBinarization[T](base: TimeSeries[T], predicate: T => Boolean) extends TimeSeries[Boolean]
{
   override def compute(times: Stream[LocalDateTime]) = base.compute(times).map { case (t,v) => (t,v.map(predicate))}
}
