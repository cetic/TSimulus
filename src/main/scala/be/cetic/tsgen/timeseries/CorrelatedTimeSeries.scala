package be.cetic.tsgen.timeseries

import org.joda.time.LocalDateTime

import scala.util.Random

/**
  * This time series is built as a correlation of an other time series.
  *
  * See http://www.sitmo.com/article/generating-correlated-random-numbers/ for explainations.
  *
  * @param base the time series generator on which this generator is based.
  * @param seed the value used as seed for the random number generator. For a fixed seed and a fixed time series,
  *             the correlated values are deterministically generated.
  * @param rho  the correlation coefficient determining the *strongness* of the correlation. Must be in [0, 1]
  */
case class CorrelatedTimeSeries(base: TimeSeries[Double],
                                seed: Int,
                                rho: Double) extends TimeSeries[Double]
{
   val rho_square = rho*rho

   override def compute(times: Stream[LocalDateTime]) =
   {
      val r = new Random(seed)
      base.compute(times)
          .map {case(t,v) => (t, v.map(a => (rho * a) + (math.sqrt(1 - rho_square) * r.nextGaussian)))}
   }
}
