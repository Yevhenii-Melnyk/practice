object HtmlBuilder extends App {

	import html._

	val result = html {
		div {
			div {
				a(href = "http://stackoverflow.com")
			}
		}
	}
}

sealed trait Node

case class Element(name: String, attrs: Map[String, String], body: Node) extends Node

case class Text(content: String) extends Node

case object Empty extends Node

object html {
	implicit val node: Node = Empty

	def apply(body: Node) = body

	def a(href: String)(implicit body: Node) =
		Element("a", Map("href" -> href), body)

	def div(body: Node) =
		Element("div", Map.empty, body)
}

object Node {
	implicit def strToText(str: String): Text = Text(str)
}

