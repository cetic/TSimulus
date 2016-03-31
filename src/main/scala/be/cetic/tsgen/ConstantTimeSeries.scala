package be.cetic.tsgen
import org.joda.time.LocalDateTime

/**
  * A time series made of a constant value.
  */
case class ConstantTimeSeries(value: Double) extends TimeSeriesGenerator
{
   override def compute(time: LocalDateTime): Double = value
}
