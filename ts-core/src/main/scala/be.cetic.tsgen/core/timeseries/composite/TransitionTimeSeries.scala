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

package be.cetic.tsgen.core.timeseries.composite

import be.cetic.tsgen.core.timeseries.TimeSeries
import com.github.nscala_time.time.Imports._
import org.joda.time.{Duration, LocalDateTime}

/**
  * A transition time series takes its values from two successive time series:
  * initially, a first time series is used for generating the desired values. At a given time,
  * a second time series is used instead of the first one.
  *
  * There may be a transition period during which values from both base time series are mixed
  * in order to produce a new value.
  *
  * If a transition is specified, its effect begins at the specified transition time.
  *
  * During the transition, if one of the base time series does not provide any value,
  * the optional value generated by the other base time series is used as it. If both base time series
  * don't generate any value, the transition time series does not generate any value.
  *
  * @param first the first base time series used to generate values.
  * @param second the second base time series used to generate values.
  * @param time the time at which the transition begins.
  * @param transition the duration of the optional transition, as well as the function that describes
  *                   how values from both base time series are mixed. The two first parameters of this function
  *                   are the values of the first and the second base time series (respectively), and the third
  *                   parameter is a value between 0 and 1 expression the status of the transition from the first
  *                   base time series (0) to the second one (1). If no transition is specified, the new time series
  *                   instantaneously prevails.
  */
case class TransitionTimeSeries[T](first: TimeSeries[T],
                                   second: TimeSeries[T],
                                   time: LocalDateTime,
                                   transition: Option[(Duration, (T,T,Double) => T)]) extends TimeSeries[T]
{
   override def compute(times: Stream[LocalDateTime]): Stream[(LocalDateTime, Option[T])] =
   {
      val vFirst = first.compute(times)
      val vSecond = second.compute(times)

      (vFirst zip vSecond).map { case (s1, s2) => {
         val t = s1._1

         val v1 = s1._2
         val v2 = s2._2

         val mixed = if(t <= time) v1
                     else
                     {
                        transition match {
                           case None => v2
                           case Some((duration, f)) => {
                              if(t > time + duration) v2
                              else // Real mixing
                              {
                                 if(v1.isEmpty) v2
                                 else if(v2.isEmpty) v1
                                 else {
                                    val ratio = new Duration(
                                       time.toDateTime(DateTimeZone.UTC),
                                       t.toDateTime(DateTimeZone.UTC)
                                    ).getMillis / duration.getMillis.toDouble

                                    Some(f(v1.get, v2.get, ratio))
                                 }
                              }
                           }
                        }
                     }

         (t, mixed)
      }}
   }
}
