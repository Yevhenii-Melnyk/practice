package crawler

import org.jsoup.Jsoup

object SpecialityParser {

  val speciality: String = "http://vstup.info/2016/92/i2016i92p301264.html#list"
  val response = Jsoup.connect(speciality).ignoreContentType(true)
    .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1")
    .get()

}
