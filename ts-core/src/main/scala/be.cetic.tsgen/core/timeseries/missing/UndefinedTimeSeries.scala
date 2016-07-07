package be.cetic.tsgen.core.timeseries.missing

import be.cetic.tsgen.core.timeseries.TimeSeries
import org.joda.time.LocalDateTime

/**
  * A time series that only produces undefined values.
  */
class UndefinedTimeSeries extends TimeSeries[Any]
{
   override def compute(times: Stream[LocalDateTime]) = times.map(t => (t, None))
}
