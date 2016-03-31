package be.cetic.tsgen
import org.joda.time.LocalDateTime

/**
  * A time series made of a constant value.
  */
case class ConstantTimeSeries(value: Double) extends ScalarTimeSeriesGenerator
{
   override def compute(time: LocalDateTime): Double = value
}
