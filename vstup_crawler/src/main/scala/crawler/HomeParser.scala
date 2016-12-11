package crawler

import org.jsoup.Jsoup
import scala.collection.JavaConverters._

case class Region(name: String, url: String)

object HomeParser extends App {

  val home: String = "http://vstup.info/"
  val response = Jsoup.connect(home).ignoreContentType(true)
    .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1")
    .get()

  val regions = response.select(".tablesaw.tablesaw-stack tr td a").asScala
    .map(e => Region(e.text, e.absUrl("href"))).toList

  println(regions)

}
