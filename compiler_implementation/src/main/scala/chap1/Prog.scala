package chap1

import scala.annotation.tailrec

object Prog extends App {

  // Maxargs
  private def maxargs(exps: ExpList): Int = {
    @tailrec
    def inner(innerExps: ExpList, count: Int): Int = innerExps match {
      case LastExpList(_) => count
      case PairExpList(_, tail) => inner(tail, count + 1)
    }

    inner(exps, 1)
  }

  private def maxargs(exp: Exp): Int = exp match {
    case OpExp(left, _, right) => math.max(maxargs(left), maxargs(right))
    case EseqExp(stm, e) => math.max(maxargs(stm), maxargs(e))
    case _ => 0
  }

  def maxargs(s: Stm): Int = s match {
    case CompoundStm(stm1, stm2) => math.max(maxargs(stm1), maxargs(stm2))
    case AssignStm(_, exp) => maxargs(exp)
    case PrintStm(exps) => maxargs(exps)
  }


  // Interpret
  def interp(s: Stm): Unit = {

    def interp(stm: Stm, table: AbstractTable): AbstractTable = stm match {
      case AssignStm(id, exp) =>
        val (res, t) = interpExp(exp, table)
        t.update(id, (res, t)._1)
      case CompoundStm(stm1, stm2) =>
        val t1 = interp(stm1, table)
        interp(stm2, t1)
      case PrintStm(expList) =>
        val (res, t1) = printExpList(expList, table)
        println(res + ";")
        t1
    }

    def interpExp(exp: Exp, table: AbstractTable): (Int, AbstractTable) = exp match {
      case IdExp(id) => (table.lookup(id), table)
      case NumExp(num) => (num, table)
      case OpExp(left, operator, right) =>
        val (res1, t1) = interpExp(left, table)
        val (res2, t2) = interpExp(right, t1)
        (oper(operator)(res1, res2), t2)
      case EseqExp(stm, e) => interpExp(e, interp(stm, table))
    }

    def oper(oper: Oper)(value1: Int, value2: Int): Int = oper match {
      case Plus => value1 + value2
      case Minus => value1 - value2
      case Times => value1 * value2
      case Div => value1 / value2
    }

    def printExpList(expList: ExpList, table: AbstractTable): (String, AbstractTable) = expList match {
      case LastExpList(head) =>
        val (res, t1) = interpExp(head, table)
        (res.toString, t1)
      case PairExpList(head, tail) =>
        val (res, t1) = interpExp(head, table)
        val (str, t2) = printExpList(tail, t1)
        (res.toString + " " + str, t2)
    }

    interp(s, Nil)

  }


  abstract class AbstractTable {

    def update(id: String, value: Int): AbstractTable = {
      Table(id, value, this)
    }

    def lookup(key: String): Int = {
      def inner(t: AbstractTable): Int = t match {
        case Table(id, value, _) if key == id => value
        case Table(_, _, tail) => inner(tail)
        case Nil => throw new IllegalArgumentException
      }

      inner(this)
    }

  }

  case class Table(id: String, value: Int, tail: AbstractTable) extends AbstractTable

  object Nil extends AbstractTable

  // a := 5+3; b := (print(a, a-1), 10*a); print(b)
  val prog = CompoundStm(
    AssignStm("a", OpExp(NumExp(5), Plus, NumExp(3))),
    CompoundStm(
      AssignStm("b",
        EseqExp(
          PrintStm(PairExpList(IdExp("a"), LastExpList(OpExp(IdExp("a"), Minus, NumExp(1))))),
          OpExp(NumExp(10), Times, IdExp("a"))
        )),
      PrintStm(LastExpList(IdExp("b")))
    ))

  assert(maxargs(prog) == 2)
  assert(maxargs(PrintStm(LastExpList(IdExp("b")))) == 1)

  assert(maxargs(CompoundStm(
    AssignStm("a", OpExp(NumExp(5), Plus, NumExp(3))),
    AssignStm("b", OpExp(NumExp(10), Times, IdExp("a")))))
    == 0)

  interp(prog)
}
