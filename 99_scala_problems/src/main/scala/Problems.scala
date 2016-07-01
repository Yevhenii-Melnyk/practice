import scala.Int
import scala.annotation.tailrec

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

object Problem3 extends App {

  def nth[A](n: Int, list: List[A]): A = list(n)

  @tailrec
  def nth2[A](n: Int, list: List[A]): A = n match {
    case 0 => list.head
    case _ => nth2(n - 1, list.tail)
  }

  @tailrec
  def nthRecursive[A](n: Int, ls: List[A]): A = (n, ls) match {
    case (0, h :: _) => h
    case (n, _ :: tail) => nthRecursive(n - 1, tail)
    case (_, Nil) => throw new NoSuchElementException
  }

  assert(nth(2, List(1, 1, 2, 3, 5, 8)) == 2)
  assert(nth2(2, List(1, 1, 2, 3, 5, 8)) == 2)
  assert(nthRecursive(2, List(1, 1, 2, 3, 5, 8)) == 2)

}

object Problem4 extends App {

  def length[A](list: List[A]): Int = list.length

  def length1[A](list: List[A]): Int = list.foldLeft(0)((x, y) => x + 1)

  assert(length(List(1, 1, 2, 3, 5, 8)) == 6)
  assert(length1(List(1, 1, 2, 3, 5, 8)) == 6)

}

object Problem5 extends App {

  def reverse[A](list: List[A]): List[A] = list.reverse

  def reverse1[A](list: List[A]): List[A] = list match {
    case Nil => Nil
    case x :: tail => reverse1(tail) ++ List(x)
  }


  def reverse2[A](list: List[A]): List[A] = {
    @tailrec
    def reverse(acc: List[A], list: List[A]): List[A] = list match {
      case Nil => acc
      case x :: tail => reverse(x :: acc, tail)
    }
    reverse(Nil, list)
  }

  def reverseFunctional[A](ls: List[A]): List[A] =
    ls.foldLeft(List[A]()) { (r, h) => h :: r }

  println(reverse(List(1, 1, 2, 3, 5, 8)))
  println(reverse1(List(1, 1, 2, 3, 5, 8)))
  println(reverse2(List(1, 1, 2, 3, 5, 8)))
  println(reverseFunctional(List(1, 1, 2, 3, 5, 8)))

}

object Problem6 extends App {

  def isPalindrome[A](list: List[A]): Boolean = list match {
    case Nil => true
    case x :: Nil => true
    case x :: (mid :+ z) if x == z => isPalindrome(mid)
    case x :: (mid :+ z) if x != z => false
  }

  assert(isPalindrome(List(1, 2, 3, 2, 1)))
  assert(isPalindrome(List(2, 3, 3, 2)))
  assert(isPalindrome(List(2)))
  assert(isPalindrome(Nil))
  assert(!isPalindrome(List(3, 2, 3, 2, 1)))

}
