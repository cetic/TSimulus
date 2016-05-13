package be.cetic.tsgen

import org.apache.commons.math3.stat.StatUtils

package object config
{
   def aggregationFunction(s: String) = {
      s match {
         case "sum" => s: Seq[Double] => s.sum
         case "product" => s: Seq[Double] => s.reduce((x,y) => x*y)
         case "min" => s: Seq[Double] => s.min
         case "max" => s: Seq[Double] => s.max
         case "mean" => s: Seq[Double] => s.sum / s.length
         case "median" => s: Seq[Double] => StatUtils.percentile(s.toArray, 50)
      }
   }
}
