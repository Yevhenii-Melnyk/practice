package crawler

import java.util.Date

import akka.actor.Actor
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.{ElasticClient, ElasticsearchClientUri}

case class IndexSpeciality(specialityMessage: SpecialityMessage, stat: Stat)

class IndexingActor extends Actor {
  val client = ElasticClient.transport(ElasticsearchClientUri("localhost", 9300))


  def indexSpeciality(spec: IndexSpeciality) {
    client.execute {
      indexInto("vstup" / "speciality") fields
        List(
          "name" -> spec.specialityMessage.speciality.specialityInfo.name.trim,
          "type" -> spec.specialityMessage.speciality.specialityInfo.specialityType.trim,
          "faculty" -> spec.specialityMessage.speciality.specialityInfo.faculty.trim,
          "free" -> spec.specialityMessage.speciality.free,
          "total" -> spec.specialityMessage.speciality.total,
          "certificateK" -> spec.specialityMessage.certificateK,
          "main" -> spec.specialityMessage.speciality.subjects.mains.map(s => Map("name" -> s.name, "k" -> s.k)),
          "optional" -> spec.specialityMessage.speciality.subjects.opts.map(s => Map("name" -> s.name, "k" -> s.k)),
          "region" -> spec.specialityMessage.region.trim,
          "timing" -> spec.specialityMessage.timing.trim,
          "university" -> Map(
            "address" -> spec.specialityMessage.universityInfo.address.trim,
            "email" -> spec.specialityMessage.universityInfo.email.trim,
            "name" -> spec.specialityMessage.universityInfo.name.trim,
            "phones" -> spec.specialityMessage.universityInfo.phones.trim,
            "postcode" -> spec.specialityMessage.universityInfo.postcode.trim,
            "site" -> spec.specialityMessage.universityInfo.site.trim,
            "type" -> spec.specialityMessage.universityInfo.uniType.trim
          ),
          "stat" -> Map(
            "max" -> spec.stat.max,
            "min" -> spec.stat.min,
            "freeMax" -> spec.stat.freeMin
          )
        )
    }
  }

  override def receive: Receive = {
    case spec: IndexSpeciality =>
      indexSpeciality(spec)
      println(new Date)
  }

}



