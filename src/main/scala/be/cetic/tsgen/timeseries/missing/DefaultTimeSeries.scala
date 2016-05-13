package be.cetic.tsgen.timeseries.missing

import be.cetic.tsgen.timeseries.TimeSeries
import org.joda.time.LocalDateTime

/**
  * A time series that produces the first defined value, among the values of
  * underlying time series.
  *
  * If no defined value is available, an undefined value is produced.
  *
  */
case class DefaultTimeSeries[T](generators: Seq[TimeSeries[T]]) extends TimeSeries[T]
{
   override def compute(times: Stream[LocalDateTime]) =
   {
      val others = generators.map(c => c.compute(times).map(Seq(_)))
                             .reduce((s1, s2) => (s1 zip s2).map(e => e._1 ++ e._2))
                             .map(seq => (seq.head._1, seq.map(_._2)))
                             .map(entry => (entry._1, entry._2.flatten) )

      others map {
         case(t: LocalDateTime, s: Seq[T]) => (t, s.headOption)
      }
   }
}
