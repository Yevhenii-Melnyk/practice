object Problem1 extends App {
  //Find the last element of a list.

  def last[T](list: List[T]) = list.last

  def last1[T](list: List[T]): T = list match {
    case x :: Nil => x
    case _ :: tail => last1(tail)
  }

  assert(last(List(1, 1, 2, 3, 5, 8)) == 8)
  assert(last1(List(1, 1, 2, 3, 5, 8)) == 8)

}

object Problem2 extends App {

  def lastBut1[T](list: List[T]): T = list.init.last


  def lastBut1Rec[T](list: List[T]): T = list match {
    case x :: _ :: Nil => x
    case _ :: tail => lastBut1Rec(tail)
  }

  def lastNthBuiltin[A](n: Int, ls: List[A]): A = {
    ls.takeRight(n).head
  }

  println(lastNthBuiltin(4, List(1, 1, 2, 3, 5, 8)))

  assert(lastBut1(List(1, 1, 2, 3, 5, 8)) == 5)
  assert(lastBut1Rec(List(1, 1, 2, 3, 5, 8)) == 5)

}
