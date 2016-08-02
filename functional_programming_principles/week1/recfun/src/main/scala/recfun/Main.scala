package recfun

import scala.annotation.tailrec
import scala.collection.immutable.Stack


object Main {
  def main(args: Array[String]) {
    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }
  }

  /**
    * Exercise 1
    */
  def pascal(c: Int, r: Int): Int = (c, r) match {
    case (x, y) if y < x => 0
    case (0, _) => 1
    case _ => pascal(c - 1, r - 1) + pascal(c, r - 1)
  }

  /**
    * Exercise 2
    */
  def balance(chars: List[Char]): Boolean = {
    def inner(stack: List[Char], chars: List[Char]): Boolean = chars match {
      case Nil => stack.isEmpty
      case x :: xs if x == '(' => inner(x :: stack, xs)
      case x :: xs if x == ')' =>
        if (stack.nonEmpty && stack.head == '(') inner(stack.tail, xs)
        else false
      case x :: xs => inner(stack, xs)
    }
    inner(List(), chars)
  }

  /**
    * Exercise 3
    */
  def countChange(money: Int, coins: List[Int]): Int = coins match {
    case Nil => 0
    case x :: xs =>
      if (money > x) countChange(money - x, coins) + countChange(money, xs)
      else if (money == x) 1 + countChange(money, xs)
      else countChange(money, xs)
  }

}
