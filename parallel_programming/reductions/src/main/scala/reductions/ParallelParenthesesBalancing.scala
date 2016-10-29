package reductions

import scala.annotation._
import org.scalameter._
import common._

object ParallelParenthesesBalancingRunner {

  @volatile var seqResult = false

  @volatile var parResult = false

  val standardConfig = config(
    Key.exec.minWarmupRuns -> 40,
    Key.exec.maxWarmupRuns -> 80,
    Key.exec.benchRuns -> 120,
    Key.verbose -> true
  ) withWarmer new Warmer.Default

  def main(args: Array[String]): Unit = {
    val length = 100000000
    val chars = new Array[Char](length)
    val threshold = 10000
    val seqtime = standardConfig measure {
      seqResult = ParallelParenthesesBalancing.balance(chars)
    }
    println(s"sequential result = $seqResult")
    println(s"sequential balancing time: $seqtime ms")

    val fjtime = standardConfig measure {
      parResult = ParallelParenthesesBalancing.parBalance(chars, threshold)
    }
    println(s"parallel result = $parResult")
    println(s"parallel balancing time: $fjtime ms")
    println(s"speedup: ${seqtime / fjtime}")
  }
}

object ParallelParenthesesBalancing extends App {

  /** Returns `true` iff the parentheses in the input `chars` are balanced.
    */
  def balance(chars: Array[Char]): Boolean = {
    var total = 0
    var least = 0
    var idx = 0
    while (idx < chars.length) {
      val char = chars(idx)
      if (char == '(') total += 1
      else if (char == ')') total -= 1
      if (total < least) least = total
      idx += 1
    }
    total + least == 0
  }

  /** Returns `true` iff the parentheses in the input `chars` are balanced.
    */
  def parBalance(chars: Array[Char], threshold: Int): Boolean = {

    def traverse(from: Int, until: Int): (Int, Int) = {
      var total = 0
      var least = 0
      var idx = from
      while (idx < until) {
        val char = chars(idx)
        if (char == '(') total += 1
        else if (char == ')') total -= 1
        if (total < least) least = total
        idx += 1
      }
      (least, total)
    }

    def reduce(from: Int, until: Int): (Int, Int) = {
      if (until - from <= threshold) traverse(from, until)
      else {
        val middle = from + (until - from) / 2
        val ((least1, total1), (least2, total2)) = parallel(reduce(from, middle), reduce(middle, until))
        (Math.min(least1, total1 + least2), total1 + total2)
      }
    }
    
    reduce(0, chars.length) ==(0, 0)
  }

  // (least, total)
  //println(parBalance("((test)(c".toCharArray, 1)) //(0,2)
  //println(parBalance("+x)-4)((()(".toCharArray, 1)) //(-2,1)
  //println(parBalance("(4)444)--".toCharArray, 1)) //(-1,-1)
  //println(parBalance("-)xx(x)x)".toCharArray, 1)) //(-2,-2)

  //println(parBalance("((test)(c+x)-4)(((".toCharArray, 1)) //(0,3)
  //println(parBalance("(4)444)---)xx(x)x)".toCharArray, 1)) //(-3,-3)

  //println(parBalance("((test)(c+x)-4)((((4)444)---)xx(x)x)".toCharArray, 1)) //(-0,-0)

  //
  //                        (0,0)
  //             ((test)(c+x)-4)((((4)444)---)xx(x)x)
  //             /                                 \
  //          (0,3)                            (-3, -3)
  //        ((test)(c+x)-4)(((            (4)444)---)xx(x)x)
  //        /             \                /            \
  //       (0,2)      (-2,1)              (-1,-1)       (-2, -2)
  //    ((test)(c     +x)-4)((()(       (4)444)--      -)xx(x)x)

  // For those who want more:
  // Prove that your reduction operator is associative!

  // f( (l1,t1),(l2,t2)) == (min(l1, t1+l2), t1+t2)
  //
  // f (f((l1,t1), (l2,t2)), (l3,t3))
  // == f((min(l1, t1+l2), t1+t2), (l3, t3))
  // == ( min( min(l1, t1+l2), (t1+t2)+l3), (t1+t2)+t3   )       `min` is associative
  // == ( min( l1, min(t1+l2,  t1+(t2+l3))  , t1+(t2+t3) )        + is monotonic  if a < b then a + c < b + c
  // == ( min(l1, t1+min(l2, t2+l3)),    t1+(t2+t3)      )
  // == f( (l1,t1), (min(l2, t2+l3), t2+t3))
  // == f( (l1,t1), f((l2,t2),(l3,t3)) )
}
