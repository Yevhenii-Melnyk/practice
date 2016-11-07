package chap1

trait Tree[+A] {

  def nothing: A = null.asInstanceOf[A]

  def insertKey(key: String): Tree[A] = this match {
    case Empty => Node(Empty, key, nothing, Empty)
    case Node(l, k, v, r) if key < k => Node(l.insertKey(key), k, v, r)
    case Node(l, k, v, r) if key > k => Node(l, k, v, r.insertKey(key))
    case Node(l, _, _, r) => Node(l, key, nothing, r)
  }


  def insert[B >: A](key: String, value: B): Tree[B] = this match {
    case Empty => Node(Empty, key, value, Empty)
    case Node(l, k, v, r) if key < k => Node[B](l.insert(key, value), k, v, r)
    case Node(l, k, v, r) if key > k => Node[B](l, k, v, r.insert(key, value))
    case Node(l, _, _, r) => Node[B](l, key, value, r)
  }

  def member(key: String): Boolean = this match {
    case Node(l, k, _, r) if k == key => true
    case Node(l, k, _, r) => l.member(key) || r.member(key)
    case Empty => false
  }

  def lookup(key: String): Option[A] = this match {
    case Node(l, k, v, r) if k == key => Some(v)
    case Node(l, k, _, r) if key < k => l.lookup(key)
    case Node(l, k, _, r) if key > k => r.lookup(key)
    case Empty => None
  }


}

case class Node[A](left: Tree[A], key: String, value: A, right: Tree[A]) extends Tree[A]

case object Empty extends Tree[Nothing]

object Tree {
  def apply[A](key: String, value: A): Tree[A] = Node(Empty, key, value, Empty)
}

object TreeMain extends App {

  private val tree = Tree("1", 1).insert("2", 2).insert("0", 0)
  assert(tree.member("0"))
  assert(tree.member("1"))
  assert(tree.member("2"))
  assert(tree.insertKey("2").member("2"))
  assert(!tree.member("4"))
  assert(tree.lookup("0").get == 0)
  assert(tree.lookup("1").get == 1)
  assert(tree.lookup("2").get == 2)
  assert(tree.lookup("4").isEmpty)

  println(Empty.insertKey("t").insertKey("s").insertKey("p").insertKey("i").insertKey("p").insertKey("f").insertKey("b").insertKey("s").insertKey("t"))
  println(Empty.insertKey("a").insertKey("b").insertKey("c").insertKey("d").insertKey("e").insertKey("f").insertKey("g").insertKey("h").insertKey("i"))

}
