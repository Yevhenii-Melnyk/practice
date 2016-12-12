package crawler

import akka.actor.{Actor, ActorRef, Props}
import akka.actor.Actor.Receive
import akka.routing.RoundRobinPool
import org.apache.commons.validator.routines.UrlValidator
import org.jsoup.Jsoup

import scala.collection.JavaConverters._
import scala.util.Try

case class Student(cert: Double, zno: Double)

case class Stat(max: Double, min: Double, freeMin: Double)

object SpecialityParser {

  val NormalInt = """([^\d]*)(\d+)""".r

  def parseInt(s: String): Int = {
    s match {
      case NormalInt(_, digits) => Integer.valueOf(digits)
      case _ => 0
    }
  }

  def parseStats(specUrl: String) = {
    //    println(specUrl)
    val response = Jsoup.connect(specUrl).ignoreContentType(true)
      .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1")
      .get()

    val total = Option(response.select("td[title='Ліцензований обсяг прийому']").first).map(_.text).map(parseInt)
    val free = Option(response.select("td[title='Обсяг державного замовлення']").first).map(_.text).map(parseInt)

    val entries = response.select("tr[title='Допущено до конкурсу']").asScala.map(e => {
      val fields = e.select("td").asScala.toArray
      Student(Try(fields(4).text.toDouble).getOrElse(0), Try(fields(5).text.toDouble).getOrElse(0))
    }).toArray

    def studSum(student: Student) = {
      student.cert + student.zno
    }

    val length = entries.length
    val stat = if (length > 0) {
      val max = studSum(entries(0))
      val min = if (total.isDefined) studSum(entries(math.min(total.get - 1, length - 1))) else -1
      val freeMin = if (free.isDefined) studSum(entries(math.min(free.get - 1, length - 1))) else -1

      Stat(max, min, freeMin)
    } else Stat(-1, -1, -1)

    stat
  }
}

case class SpecialityMessage(region: String,
                             universityInfo: UniversityInfo,
                             speciality: Speciality,
                             timing: String,
                             certificateK: Double)

class SpecialityActor extends Actor {

  val indexingActor: ActorRef = context.actorOf(Props[IndexingActor], "indexing")
  val urlValidator = new UrlValidator()

  override def receive: Receive = {
    case speciality@SpecialityMessage(_, _, s, _, _) =>
      if (urlValidator.isValid(s.url)) {
        val stat = SpecialityParser.parseStats(s.url)
        indexingActor ! IndexSpeciality(speciality, stat)
      }
  }

}