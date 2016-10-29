package reductions

import java.util.concurrent._
import scala.collection._
import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import common._

import ParallelParenthesesBalancing._

@RunWith(classOf[JUnitRunner])
class ParallelParenthesesBalancingSuite extends FunSuite {


  test("balance should work for any string") {
    def check(input: String, expected: Boolean) =
      assert(balance(input.toArray) == expected,
        s"balance($input) should be $expected")

    check("", true)
    check("(", false)
    check(")", false)
    check(".", true)
    check("()", true)
    check(")(", false)
    check("((", false)
    check("))", false)
    check(".)", false)
    check(".(", false)
    check("(.", false)
    check(").", false)
    check(")))).", false)
    check("(((()))).", true)
    check("(((())())).", true)
    check("(((()())())).", true)
    check("))(((", false)
    check("((((())())).", false)
    check("()(((()()))).", true)
    check("((test)(c+x)-4)((((4)444)---)xx(x)x)", true)
  }

  test("parBalance should work for any string") {
    def check(input: String, expected: Boolean) =
      assert(parBalance(input.toArray, 2) == expected,
        s"balance($input) should be $expected")

    check("", true)
    check("(", false)
    check(")", false)
    check(".", true)
    check("()", true)
    check(")(", false)
    check("((", false)
    check("))", false)
    check(".)", false)
    check(".(", false)
    check("(.", false)
    check(").", false)
    check(")))).", false)
    check("(((()))).", true)
    check("(((())())).", true)
    check("(((()())())).", true)
    check("))(((", false)
    check("((((())())).", false)
    check("((test)(c+x)-4)((((4)444)---)xx(x)x)", true)
  }


}