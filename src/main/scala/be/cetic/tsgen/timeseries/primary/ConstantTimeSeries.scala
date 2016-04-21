package be.cetic.tsgen.timeseries.primary

import be.cetic.tsgen.timeseries.IndependantTimeSeries
import org.joda.time.LocalDateTime

/**
  * A time series made of a constant value.
  */
case class ConstantTimeSeries(value: Double) extends IndependantTimeSeries[Double]
{
   override def compute(time: LocalDateTime) = Some(value)
}
