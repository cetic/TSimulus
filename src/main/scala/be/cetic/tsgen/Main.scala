package be.cetic.tsgen

object Main
{
  def main(args: Array[String]) {

    val model = ARMA(std=0.1, c=0)

    model.series take 500 foreach(e => println(e.formatted("%2f")))
  }
}
