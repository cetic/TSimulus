package be.cetic.tsgen

import org.joda.time.{DateTimeZone, Duration, LocalDateTime}

/**
  * A time series based on an ARMA model.
  *
  * The ARMA model provides a series of discrete values. In order to bind them to a particular date time,
  * a linear regression is used.
  *
  * @param arma the ARMA model used to generate a time series based on a random walk.
  * @param timeStep the duration between two consecutive steps.
  */
case class RandomWalkTimeSeries(arma: ARMA, timeStep: Duration) extends TimeSeriesGenerator[Double]
{
   override def compute(times: Stream[LocalDateTime]): Stream[Double] =
   {
      val deltaT = timeStep.getMillis.toDouble

      val timeInterval = intervals(computeTimes(times.head))
      val valueInterval = intervals(arma.series)

      val data = timeInterval zip valueInterval

      def process(times: Stream[LocalDateTime],
                  data: Stream[((LocalDateTime, LocalDateTime) , (Double, Double))]) : Stream[Double] = times match {
         case Stream.Empty => Stream.empty
         case time #:: timeRest =>
         {
            val time = times.head
            val dataRest = data.dropWhile({ case ((timeStart: LocalDateTime, timeEnd: LocalDateTime),
            (valStart: Double, valEnd: Double)) => time isAfter timeEnd
            })

            val tStart = dataRest.head._1._1
            val tEnd = dataRest.head._1._2

            val vStart = dataRest.head._2._1
            val vEnd = dataRest.head._2._2

            val timeRatio = new Duration(tStart.toDateTime(DateTimeZone.UTC),
                                         time.toDateTime(DateTimeZone.UTC)).getMillis / deltaT

            val value = vStart + (vEnd - vStart) * timeRatio

            value #:: process(timeRest, dataRest)
         }
      }

      process(times, data)
   }

   /**
     * Generate a stream of the datetimes at which a random value is generated
     * @param time
     * @return
     */
   private def computeTimes(time: LocalDateTime) :Stream[LocalDateTime] = time #:: computeTimes(time plus timeStep)

   private def intervals[T](xs: Stream[T]) = xs zip xs.tail
}
