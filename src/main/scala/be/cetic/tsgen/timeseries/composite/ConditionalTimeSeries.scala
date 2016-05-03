package be.cetic.tsgen.timeseries.composite

import be.cetic.tsgen.timeseries.TimeSeries
import org.joda.time.LocalDateTime

/**
  * A time series based on an underlying time series. The values of the underlying time series
  * are forwarded by this time series iff a binary value from an other time series is true.
  *
  * If the value of the underlying time series is not defined, then the value of this time series is not defined.
  * If the value of the binary time series is not defined, then the value of this time series is not defined.
  *
  * @param generator the underlying time series on which this time series is based.
  * @param binary the binary time series used for determining if the values of
  *               the underlying time series must be forwarded.
  */
case class ConditionalTimeSeries[T](generator: TimeSeries[T], binary: TimeSeries[Boolean]) extends TimeSeries[T]
{
   override def compute(times: Stream[LocalDateTime]) =
   {
      (generator.compute(times) zip binary.compute(times)).map { case ((t1, v),(t2,b)) => {
         assert(t1 == t2)

         if(v.isEmpty || !b.getOrElse(false)) (t1,None)
         else (t1, v)
      }}
   }
}
