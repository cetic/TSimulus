package be.cetic.tsgen.core.timeseries.binary

import be.cetic.tsgen.core.timeseries.TimeSeries
import org.joda.time.LocalDateTime

/**
  * A binary time series for which all values are true.
  */
class TrueTimeSeries extends TimeSeries[Boolean]
{
   override def compute(times: Stream[LocalDateTime]) = times.map(t => (t, Some(true)))
}
