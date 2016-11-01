package chap1

// Statement
sealed abstract class Stm

case class CompoundStm(stm1: Stm, stm2: Stm) extends Stm

case class AssignStm(id: String, exp: Exp) extends Stm

case class PrintStm(exps: ExpList) extends Stm

// Expression
sealed abstract class Exp

case class IdExp(id: String) extends Exp

case class NumExp(num: Int) extends Exp

case class OpExp(left: Exp, oper: Oper, right: Exp) extends Exp

case class EseqExp(stm: Stm, exp: Exp) extends Exp

// Expression list
sealed abstract class ExpList

case class PairExpList(head: Exp, tail: ExpList) extends ExpList

case class LastExpList(head: Exp) extends ExpList

// Operation
sealed abstract class Oper

object Plus extends Oper

object Minus extends Oper

object Times extends Oper

object Div extends Oper