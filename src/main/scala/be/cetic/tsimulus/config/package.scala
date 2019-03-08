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

package be.cetic.tsimulus

import org.apache.commons.math3.stat.StatUtils

package object config {
  def aggregationFunction[U](s: String): Seq[Double] => U = {
    s match {
      case "sum" => s: Seq[Double] => s.sum
      case "product" => s: Seq[Double] => s.reduce((x, y) => x * y)
      case "min" => s: Seq[Double] => s.min
      case "max" => s: Seq[Double] => s.max
      case "mean" => s: Seq[Double] => s.sum / s.length
      case "median" => s: Seq[Double] => StatUtils.percentile(s.toArray, 50)
      case "identity" => (s: Seq[Double]) => s
    }
  }.asInstanceOf[Seq[Double] => U]
}
