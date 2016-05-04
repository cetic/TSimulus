package be.cetic.tsgen.timeseries.primary

import be.cetic.tsgen.timeseries.TimeSeries
import org.joda.time.LocalDateTime

/**
  * A time series that only have undefined values.
  */
class UndefinedTimeSeries extends TimeSeries[Any]
{
   override def compute(times: Stream[LocalDateTime]) = times.map(t => (t,None))
}
