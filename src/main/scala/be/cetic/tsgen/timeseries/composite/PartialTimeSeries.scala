package be.cetic.tsgen.timeseries.composite

import be.cetic.tsgen.timeseries.TimeSeries
import com.github.nscala_time.time.Imports._
import org.joda.time.LocalDateTime

import scala.util.Random

/**
  * A time series that only product values during a given period.
  *
  * @param base The time series on which this time series is based.
  * @param from The date from which values are produced.
  * @param to The date to which which values are produced.
  * @param missingRate The frequency at which a value is not produced, despite the fact that
  *                    it should be produced according to the specified limit dates.
  */
case class PartialTimeSeries[T](base: TimeSeries[T],
                                from: Option[LocalDateTime],
                                to: Option[LocalDateTime],
                                missingRate: Option[Double]) extends TimeSeries[T]
{
   override def compute(times: Stream[LocalDateTime]) = {

      base.compute(times).map {case (t,v) => {
            if(v.isEmpty) (t,v)
            else
            {
               if(from.isDefined && t < from.get) (t, None)
               else if(to.isDefined && t > to.get) (t, None)
               else missingRate match {
                  case None => (t,v)
                  case Some(odds) => if(Random.nextDouble() < odds) (t, None)
                                     else (t, v)
               }
            }
         }
      }
   }
}
