package be.cetic.tsgen.timeseries
import org.joda.time.LocalDateTime

/**
  * A binary time series based on two time series. This time series is true iff both
  * base time series are true.
  *
  * If the value is not defined for at least one of the base time series, then the AND value is not defined.
  */
case class AndTimeSeries(a: TimeSeries[Boolean], b: TimeSeries[Boolean]) extends TimeSeries[Boolean]
{
   /**
     * @param times a series of time for which values must be computed. Each time must be greater than or equal to
     *              the previous one.
     * @return the values associated to the specified times, if specified.
     */
   override def compute(times: Stream[LocalDateTime]): Stream[(LocalDateTime, Option[Boolean])] =
   {
      val aSeries = a.compute(times)
      val bSeries = b.compute(times)

      (aSeries zip bSeries).map { case (x,y) => {
         val time = x._1
         assert(time equals y._2)

         val value = if(x._2.isEmpty || y._2.isEmpty) None
                     else Some(x._2.get && y._2.get)

         (time, value)
      }}
   }
}
