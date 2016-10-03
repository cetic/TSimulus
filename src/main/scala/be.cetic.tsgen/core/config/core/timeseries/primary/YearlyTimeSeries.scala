/*
 * Copyright 2106 Cetic ASBL
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package be.cetic.tsgen.core.config.core.timeseries.primary

import be.cetic.tsgen.core.config.core.timeseries.IndependantTimeSeries
import com.github.nscala_time.time.Imports._
import org.apache.commons.math3.analysis.interpolation.AkimaSplineInterpolator
import org.joda.time.Seconds

/**
  * Represents cyclic variation of a time series on a yearly basis.
  * Since years are on an open scale, a smooth interpolation of values is not waranted.
  *
  * @param controlPoints The value a time series must pass by at a given time.
  */
case class YearlyTimeSeries(controlPoints: Map[Int, Double]) extends IndependantTimeSeries[Double]
{
   private val beginning = controlPoints.keys.min
   private val end = controlPoints.keys.max
   private val length = new Period(year_threshold(beginning), year_threshold(end))

   /**
     * @param year A year.
     * @return The datetime that define the limit of the year.
     */
   private def year_threshold(year : Int): LocalDateTime =
   {
      val begining = new LocalDateTime(year, 1, 1, 0, 0, 0)
      val end = new LocalDateTime(year, 12, 31, 23, 59, 59)

      val duration = new Duration(begining.toDateTime(DateTimeZone.UTC), end.toDateTime(DateTimeZone.UTC))
      val half_duration = duration / 2

      return begining + half_duration
   }

   val interpolator =
   {

      val entries = controlPoints.map { case (key, value) => ((key-1).toDouble, value)}.toSeq.sortBy(entry => entry._1)
                                 .sortBy(_._1)

      val tempo_date = entries.map(_._1)
      val before_date = tempo_date.head - 1
      val penultimate_date = tempo_date.head - 2
      val after_date = tempo_date.last + 1
      val after_after_date = tempo_date.last + 2

      val dates = (penultimate_date +: before_date +: tempo_date :+ after_date :+ after_after_date).toArray

      val tempo_values = entries.map(_._2)
      val values = (tempo_values.takeRight(2).head +: tempo_values.last +: tempo_values :+ tempo_values.head :+ tempo_values(1)).toArray

      new AkimaSplineInterpolator().interpolate(dates, values)
   }

   private def correctedTime(time: LocalDateTime): LocalDateTime =
   {
      if (time < year_threshold(beginning-1)) return correctedTime(time + length)
      if (time > year_threshold(end+1)) return correctedTime(time - length)
      return time
   }

   def compute(time: LocalDateTime): Option[Double] =
   {
      val current_time = correctedTime(time)
      val current_year = current_time.getYear

      val active_year = if(current_time < year_threshold(current_year)) current_year - 1
                        else current_year


      val next_year = active_year + 1

      val max_duration = Seconds.secondsBetween(year_threshold(active_year), year_threshold(next_year))
      val current_duration = Seconds.secondsBetween(year_threshold(active_year), current_time)

      val ratio = current_duration.getSeconds.toDouble / max_duration.getSeconds

      return Some(interpolator.value(active_year + ratio))
   }
}
