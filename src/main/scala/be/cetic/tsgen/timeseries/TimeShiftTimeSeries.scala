package be.cetic.tsgen.timeseries

import org.joda.time.{Duration, LocalDateTime}
import com.github.nscala_time.time.Imports._

/**
  * A time series based on an other time series, and for which time is shifted.
  * @param generator The base time series.
  * @param shift The time shift to apply, so that this.compute(t) == generator.compute(t+shift);
  * @tparam T
  */
case class TimeShiftTimeSeries[T](generator: TimeSeries[T], shift: Duration) extends TimeSeries[T]
{
   override def compute(times: Stream[LocalDateTime]): Stream[(LocalDateTime, Option[T])] =
      generator.compute(times.map(t => t + shift))
}