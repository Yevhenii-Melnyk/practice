package crawler

import akka.actor.{Actor, Props}
import akka.routing.RoundRobinPool
import org.jsoup.Jsoup

import scala.collection.JavaConverters._


case class University(name: String, url: String)

object RegionParser {
//  println(getUniversities("http://vstup.info/2016/i2016o21.html#reg"))

  def getUniversities(region: String) = {
    val response = Jsoup.connect(region).ignoreContentType(true)
      .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1")
      .get()

    val universities = response.select(".tablesaw.tablesaw-stack tr td a").asScala
      .map(e => University(e.text, e.absUrl("href"))).toList
    universities
  }

}

class RegionActor extends Actor {

  val universityActor = context actorOf Props(new UniversityActor).withRouter(RoundRobinPool(15))

  override def receive: Receive = {
    case Region(name, url) =>
      val universities = RegionParser.getUniversities(url)
      universities.foreach(u => universityActor ! UniversityMessage(name, u))
  }
}
