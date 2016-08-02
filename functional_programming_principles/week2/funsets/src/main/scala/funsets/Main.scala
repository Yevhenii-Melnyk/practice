package funsets

object Main extends App {

  import FunSets._

  val ints: List[Int] = List(1, 5, 6)
  println(FunSets.toString(map(ints.contains(_), _ * 2)))
}

