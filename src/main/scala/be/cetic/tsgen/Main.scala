package be.cetic.tsgen

object Main
{
  def main(args: Array[String]) {
    ARMA.series(phi=Array(0.5), theta=Array(0.5), std=0.5, c=0).take(500) foreach(e => println(e.formatted("%2f")))
  }
}
