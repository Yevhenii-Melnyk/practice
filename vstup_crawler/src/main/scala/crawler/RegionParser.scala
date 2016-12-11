package crawler

import org.jsoup.Jsoup

import scala.collection.JavaConverters._


case class University(name: String, url: String)

object RegionParser extends App {

  val region: String = "http://vstup.info/2016/i2016o21.html#reg"
  val response = Jsoup.connect(region).ignoreContentType(true)
    .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1")
    .get()

  val universities = response.select(".tablesaw.tablesaw-stack tr td a").asScala
    .map(e => University(e.text, e.absUrl("href"))).toList

  println(universities)

}
