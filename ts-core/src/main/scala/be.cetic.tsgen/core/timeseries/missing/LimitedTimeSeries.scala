package be.cetic.tsgen.core.timeseries.missing

import be.cetic.tsgen.core.timeseries.TimeSeries
import com.github.nscala_time.time.Imports._
import org.joda.time.LocalDateTime


/**
  * A time series in which there is a "hole": no value are generated while in the hole.
  * When not in the hole, the generated values are provided by a base time series.
  */
case class LimitedTimeSeries[T] (base: TimeSeries[T], from: Option[LocalDateTime], to: Option[LocalDateTime]) extends TimeSeries[T]
{
   override def compute(times: Stream[LocalDateTime]): Stream[(LocalDateTime, Option[T])] =
   {
      base.compute(times).map {case (t,v) => {

         val fromCondition = from match {
            case None => true
            case Some(x) => t >= x
         }

         val toCondition = to match {
            case None => true
            case Some(x) => t <= x
         }

         val modified = if(fromCondition && toCondition) None
                        else v

         (t, modified)
      }}
   }
}
