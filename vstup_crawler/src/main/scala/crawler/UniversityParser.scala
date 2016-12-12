package crawler

import akka.actor.{Actor, Props}
import akka.routing.RoundRobinPool
import org.jsoup.Jsoup
import org.jsoup.nodes.{Element, TextNode}

import scala.collection.JavaConverters._

case class UniversityInfo(name: String,
                          uniType: String,
                          postcode: String,
                          address: String,
                          phones: String,
                          site: String,
                          email: String)

case class Subject(name: String, k: Double)

case class SpecialitySubjects(mains: List[Subject], opts: List[Subject])

case class SpecialityInfo(name: String, faculty: String, specialityType: String)

case class Speciality(specialityInfo: SpecialityInfo, url: String, total: Int, free: Int, subjects: SpecialitySubjects)

object UniversityParser {

  def infoValue(e: Element) = {
    e.select("td").get(1).text.trim
  }

  def parseInfo(e: Element) = {
    val info = e.select("#about tr")
    val name = infoValue(info.get(0))
    val uniType = infoValue(info.get(1))
    val postcode = infoValue(info.get(2))
    val address = infoValue(info.get(3))
    val phones = infoValue(info.get(4))
    val site = infoValue(info.get(5))
    val email = infoValue(info.get(6))
    UniversityInfo(name, uniType, postcode, address, phones, site, email)
  }

  val common = """([\p{IsCyrillic}\s]+)\(k=(\d+.?\d*)\)""".r
  val optionals = """Іноземна мова\s*\((.*)\)\s*\(k=(\d+.?\d*)\)""".r
  val language = """\p{IsCyrillic}+ мова""".r

  def findCommonSubject(s: String) = common.findFirstMatchIn(s).map(m => {
    Subject(m.group(1).trim.toLowerCase, Option(m.group(2)).map(_.toDouble).getOrElse(0d))
  })

  def parseOptionalSubjects(s: String) = {
    val optionalSubjects = optionals.findFirstMatchIn(s).toList.flatMap(m => {
      val k = Option(m.group(2)).map(_.toDouble).getOrElse(0d)
      m.group(1).split(",").map(_.trim).map(_.toLowerCase).map(n => Subject(n, k)).toList
    })
    val orSubjects = common.findAllMatchIn(s).filter(m => m.group(1).trim.nonEmpty)
      .filter(m => !m.group(1).toLowerCase.contains("іноземна"))
      .map(m => {
        Subject(m.group(1).replace("або", "").trim.toLowerCase, Option(m.group(2)).map(_.toDouble).getOrElse(0d))
      }).toList
    (optionalSubjects ++ orSubjects).distinct
  }

  def parseSubjects(s: String) = {
    val parts = s.split("<br>").sorted
    val length = parts.length
    if (length == 3) {
      val mainSubject1 = findCommonSubject(parts(0)).toList
      val mainSubject2 = findCommonSubject(parts(1)).toList
      val optionalSubjects = parseOptionalSubjects(parts(2))
      SpecialitySubjects(mainSubject1 ++ mainSubject2, optionalSubjects)
    } else if (length == 2) {
      val mainSubject1 = findCommonSubject(parts(0)).toList
      val optionalSubjects = parseOptionalSubjects(parts(1))
      SpecialitySubjects(mainSubject1, optionalSubjects)
    } else if (length == 1) {
      val mainSubject1 = findCommonSubject(parts(0)).toList
      SpecialitySubjects(mainSubject1, List())
    } else {
      SpecialitySubjects(List(), List())
    }
  }

  val NormalInt = """([^\d]*)(\d+)""".r

  def parseInt(s: String): Int = {
    s match {
      case NormalInt(_, digits) => Integer.valueOf(digits)
      case _ => 0
    }
  }

  val specialityTypes = List("бакалавр", "спеціаліст", "магістр")
  val facultyPattern = """факультет:\s*(.+)""".r
  val Faculty = "факультет:"

  def parseSpecialityInfo(parts: Array[String]) = {
    val length = parts.length
    //    println("part(0)   " + parts(0))
    //    println("specialityTypes   " + specialityTypes)
    val specialityType = if (length > 0 && parts(0) != null) specialityTypes.find(sp => parts(0).contains(sp)).get else ""
//    println(parts(1))
    val faculty = if (length > 1) {
      if (parts(1).contains(Faculty))
        parts(1).replace(Faculty, "").replace(",", "").trim
      else if (length > 2) parts(2).replace(Faculty, "").replace(",", "").trim
      else ""
    } else ""

    val specialityName = if (length > 2) {
      if (parts(1).contains(Faculty))
        parts(2).trim
      else parts(1).trim
    } else ""

    SpecialityInfo(specialityName, faculty, specialityType)
  }

  def parseSpeciality(e: Element) = {
    val infoNodes = e.select("td").first.childNodes.asScala.toArray.filter(_.isInstanceOf[TextNode]).map(_.toString).map(_.toLowerCase)
    val specialityInfo = parseSpecialityInfo(infoNodes)
    //    println(specialityInfo)
    val url = Option(e.select(".button.button-mini").first).map(_.absUrl("href")).getOrElse("")
    //    println(url)
    val total = parseInt(Option(e.select("nobr[title='Ліцензований обсяг прийому']").first).map(_.text).getOrElse("0"))
    //    println(total)
    val free = parseInt(Option(e.select("nobr[title='Обсяг державного замовлення']").first).map(_.text).getOrElse("0"))
    //    println(free)
    val subjects = parseSubjects(e.select("td").last.toString)
    //    println(subjects)
    //    println("~~~~~~~~~~~~~~")
    Speciality(specialityInfo, url, total, free, subjects)
  }

  //  val subjects = """1. Математика (k=0.45)<br>2. Українська мова та література (k=0.2)<br>3. Іноземна мова (Англійська мова, Французька мова, Німецька мова, Іспанська мова) (k=0.2) або Фізика (k=0.2)"""
  //  val subjects2 = """1. Біологія (k=0.4)<br>2. Українська мова та література (k=0.2)<br>3. Історія України (k=0.25) або Математика (k=0.25) або Географія (k=0.25)"""
  //  val subjects3 = """1. Біологія (k=0.4)<br>2. Українська мова та література (k=0.2)<br>3. Іноземна мова (Англійська мова, Французька мова, Німецька мова, Іспанська мова) (k=0.35) або Історія України (k=0.35) або Географія (k=0.35)"""
  //  val subjects4 = """1. Математика (k=0.45)<br>2. Російська мова (k=0.45)"""
  //  val subjects5 = """1. Фаховий іспит<br>2. Іноземна мова (Англійська мова, Французька мова, Німецька мова, Іспанська мова)"""
  //  println(parseSubjects(subjects))
  //  println("~~~~~~~~~~~~~~~")
  //  println(parseSubjects(subjects2))
  //  println("~~~~~~~~~~~~~~~")
  //  println(parseSubjects(subjects3))
  //  println("~~~~~~~~~~~~~~~")
  //  println(parseSubjects(subjects4))
  //  println("~~~~~~~~~~~~~~~")
  //  println(parseSubjects(subjects5))

  val vnz: String = "http://vstup.info/2016/i2016i92.html#vnz"

  def parseUniversity(vnz: String) = {
    val response = Jsoup.connect(vnz).ignoreContentType(true)
      .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1")
      .get()

    val info = parseInfo(response)

    val groups = response.select(".accordion-group")

    val dailySpecialities = if (groups.size > 1) {
      val daily = groups.get(1)
      daily.select("tbody tr").asScala.map(parseSpeciality)
        .filter(s => "бакалавр".equalsIgnoreCase(s.specialityInfo.specialityType))
        .filter(_.subjects.mains.nonEmpty)
        .filter(_.subjects.opts.nonEmpty)
        .toList
    } else List()

    val extraSpecialities = if (groups.size > 2) {
      val extra = groups.get(2)
      extra.select("tbody tr").asScala.map(parseSpeciality)
        .filter(s => "бакалавр".equalsIgnoreCase(s.specialityInfo.specialityType))
        .filter(_.subjects.mains.nonEmpty)
        .filter(_.subjects.opts.nonEmpty)
        .toList
    } else List()

    val nightSpecialities = if (groups.size > 3) {
      val night = groups.get(3)
      night.select("tbody tr").asScala.map(parseSpeciality)
        .filter(s => "бакалавр".equalsIgnoreCase(s.specialityInfo.specialityType))
        .filter(_.subjects.mains.nonEmpty)
        .filter(_.subjects.opts.nonEmpty)
        .toList
    } else List()

    (info, dailySpecialities, extraSpecialities, nightSpecialities)
  }

}

case class UniversityMessage(regionName: String, university: University)

class UniversityActor extends Actor {

  val universityActor = context actorOf Props(new SpecialityActor).withRouter(RoundRobinPool(30))

  override def receive: Receive = {
    case UniversityMessage(regionName, university@University(_, url)) =>
      val (info, dailySpecialities, extraSpecialities, nightSpecialities) = UniversityParser.parseUniversity(url)

      dailySpecialities.foreach(s => sendSpec(s, regionName, info, "daily"))
      extraSpecialities.foreach(s => sendSpec(s, regionName, info, "extra"))
      nightSpecialities.foreach(s => sendSpec(s, regionName, info, "night"))
  }

  def sendSpec(s: Speciality, regionName: String, info: UniversityInfo, timing: String): Unit = {
    val optK = s.subjects.opts.head.k
    val mainK = s.subjects.mains.map(_.k).sum
    val certificateK = 1 - optK - mainK
    universityActor ! SpecialityMessage(regionName, info, s, timing, certificateK)
  }

}