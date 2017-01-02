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

package be.cetic.tsimulus.timeseries.primary

import be.cetic.tsimulus.timeseries.IndependantTimeSeries
import com.github.nscala_time.time.Imports._
import org.apache.commons.math3.analysis.interpolation.AkimaSplineInterpolator
import org.joda.time.Seconds

/**
  * Represents cyclic variation of a time series on a monthly basis.
  *
  * @param controlPoints The value a time series must pass by at a given time.
  */
case class MonthlyTimeSeries(controlPoints: Map[Int, Double]) extends IndependantTimeSeries[Double]
{
   /**
     * @param month A month in a particular year.
     * @return The datetime that defines the middle of the month.
     */
   private def month_threshold(month : YearMonth): LocalDateTime =
   {
      val beginning = month.toLocalDate(1).toLocalDateTime(new LocalTime(0, 0, 0))
      val end = (beginning + 1.month - 1.day) withTime(23,59,49,999)

      val duration = new Duration(beginning.toDateTime(DateTimeZone.UTC), end.toDateTime(DateTimeZone.UTC))
      val half_duration = duration / 2

      beginning + half_duration
   }

   val interpolator = {

      val entries = controlPoints.map { case (key, value) => ((key-1).toDouble, value)}.toSeq.sortBy(entry => entry._1)
      val dates = entries.map(_._1)
      val dates_before = dates.takeRight(2).map(d => -(11 - d + 1))
      val dates_after = dates.take(2).map(d => d+12)
      val final_dates = dates_before ++ dates ++ dates_after

      val data = entries.map(_._2)
      val data_before = data.takeRight(2)
      val data_after = data.take(2)
      val final_data = data_before ++ data ++ data_after
      
      new AkimaSplineInterpolator().interpolate(final_dates.toArray, final_data.toArray)
   }

   def compute(time: LocalDateTime): Option[Double] = {


      val current_year_month = new YearMonth(time.getYear, time.getMonthOfYear)

      val active_year_month = if(time < month_threshold(current_year_month)) current_year_month minus 1.month
                              else current_year_month

      val next_year_month = active_year_month plus 1.month

      val max_duration = Seconds.secondsBetween(month_threshold(active_year_month), month_threshold(next_year_month))
      val current_duration = Seconds.secondsBetween(month_threshold(active_year_month), time)

      val ratio = current_duration.getSeconds.toDouble / max_duration.getSeconds

      Some(interpolator.value((active_year_month.getMonthOfYear - 1) + ratio))
   }
}
