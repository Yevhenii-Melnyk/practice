package reductions

import common._

object Test extends App {


  def scanLeft[A](in: Array[A], a0: A, f: (A, A) => A, out: Array[A]): Unit = {
    out(0) = a0
    var i: Int = 0
    while (i < in.length) {
      out(i + 1) = f(out(i), in(i))
      i += 1
    }
  }

  def mapSeq[A, B](in: Array[A], left: Int, right: Int, fi: (Int, A) => B, out: Array[B]): Unit = {
    var i: Int = left
    while (i < right) {
      out(i) = fi(i, in(i))
      i += 1
    }
  }

  def reduceSeq[A](in: Array[A], left: Int, right: Int, a0: A, f: (A, A) => A): A = {
    var i: Int = left
    var res = a0
    while (i < right) {
      res = f(res, in(i))
      i += 1
    }
    res
  }

  def scanLeft2[A](in: Array[A], a0: A, f: (A, A) => A, out: Array[A]): Unit = {
    val fi = (i: Int, x: A) => reduceSeq(in, 0, i, a0, f)
    mapSeq(in, 0, in.length, fi, out)
    val last = in.length - 1
    out(last + 1) = f(out(last), in(last))
  }

  val in = Array(1, 2, 3, 4)
  val out1 = new Array[Int](in.length + 1)
  scanLeft[Int](in, 10, _ + _, out1)
  println(out1.toList)

  val out2 = new Array[Int](in.length + 1)
  scanLeft2[Int](in, 10, _ + _, out2)
  println(out2.toList)

  def reduceRes[A](t: Tree[A], f: (A, A) => A): TreeRes[A] = t match {
    case Leaf(value) => LeafRes(value)
    case Node(left, right) =>
      val (l, r) = (reduceRes(left, f), reduceRes(right, f))
      NodeRes(l, f(l.res, r.res), r)
  }

  def upSweep[A](t: Tree[A], f: (A, A) => A): TreeRes[A] = t match {
    case Leaf(value) => LeafRes(value)
    case Node(left, right) =>
      val (l, r) = parallel(reduceRes(left, f), reduceRes(right, f))
      NodeRes(l, f(l.res, r.res), r)
  }

  def downSweep[A](t: TreeRes[A], a0: A, f: (A, A) => A): Tree[A] = t match {
    case LeafRes(res) => Leaf(f(a0, res))
    case NodeRes(l, _, r) =>
      val (left, right) = parallel(downSweep(l, a0, f), downSweep(r, f(a0, l.res), f))
      Node(left, right)
  }

  println(reduceRes[Int](Node(Node(Leaf(1), Leaf(2)), Leaf(3)), _ + _))
  println(upSweep[Int](Node(Node(Leaf(1), Leaf(2)), Leaf(3)), _ + _))

  private val treeRes: TreeRes[Int] = upSweep[Int](Node(Node(Leaf(1), Leaf(2)), Node(Leaf(3), Leaf(4))), _ + _)
  println(downSweep[Int](treeRes, 10, _ + _))

}

sealed abstract class Tree[A]

case class Leaf[A](a: A) extends Tree[A]

case class Node[A](l: Tree[A], r: Tree[A]) extends Tree[A]

sealed abstract class TreeRes[A] {
  val res: A
}

case class LeafRes[A](override val res: A) extends TreeRes[A]

case class NodeRes[A](l: TreeRes[A], override val res: A, r: TreeRes[A]) extends TreeRes[A]
