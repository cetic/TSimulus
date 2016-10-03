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

package be.cetic.rtsgen.timeseries.primary

import scala.util.Random

/**
  * An auto-regressive, moving-average model.
  *
  * It provides a weakly stationary stochastic process as a sum of two polynomials
  * - one for the auto-regression
  * - one for the moving average
  *
  * The time series is built as follows:
  *
  * X_t = c + epsilon_t + ∑(phi_i * X_(t-1)) + ∑(theta_i * epsilon_(t-1))
  *
  * Where epsilon is generated from a white noise of a specified standard deviation.
  *
  * @param phi   the parameters used to characterize the autoregression part of the model
  * @param theta the parameters used to characterize the moving average part of the model
  * @param std   the standard deviation used to characterize the generated white noise
  * @param c     a constant
  * @param seed  the seed used to generate the white noise. For a given seed, the process is deterministic
  */
case class ARMA(val phi: Array[Double] = Array(),
                val theta: Array[Double] = Array(),
                val std: Double = 1,
                val c: Double = 0,
                val seed: Long = Random.nextLong)
{
   /**
     * Generates a sequence of values using a Random path progress, and based on the specified ARMA parameters.
     *
     * @return A sequence of values representing a discrete time series.
     */
   def series: Stream[Double] =
   {
      def rec_generate(previous_value: Double, previous_epsilon: Double, r: Random): Stream[Double] =
      {
         val new_epsilon = r.nextGaussian() * std
         val sum_phi = phi.fold(0D)({ (old, next) => old + (next * previous_value) })
         val sum_theta = theta.fold(0D)({ (old, next) => old + (next * previous_epsilon) })

         val ret = c + new_epsilon + sum_phi + sum_theta

         return ret #:: rec_generate(ret, new_epsilon, r)
      }

      return rec_generate(0D, 0D, new Random(seed))
   }
}

/**
  * Auto-regressive model is a series model in which the output variable depends linearly
  * on its own previous values and on a stochastic term.
  */
object AR
{
   def apply(phi: Array[Double],
             std: Double,
             c: Double,
             seed: Long = Random.nextLong) = new ARMA(phi = phi, std = std, c = c, seed = seed)
}

/**
  * A moving-average model is a series model in which values are a linear regression of the
  * current value of the series against current and previous white noise error terms.
  */
object MA
{
   def apply(theta: Array[Double],
             std: Double,
             c: Double,
             seed: Long = Random.nextLong) = new ARMA(theta = theta, std = std, c = c, seed = seed)
}
