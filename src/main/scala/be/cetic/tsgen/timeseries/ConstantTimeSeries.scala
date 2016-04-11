package be.cetic.tsgen.timeseries

import org.joda.time.LocalDateTime

/**
  * A time series made of a constant value.
  */
case class ConstantTimeSeries(value: Double) extends IndependantTimeSeries[Double]
{
   override def compute(time: LocalDateTime) = Some(value)
}
