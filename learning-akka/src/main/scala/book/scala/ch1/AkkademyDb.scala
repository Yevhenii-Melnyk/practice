package book.scala.ch1

import akka.actor.Actor
import akka.event.Logging
import book.scala.messages.SetRequest

import scala.collection.mutable

class AkkademyDb extends Actor {

  val map = new mutable.HashMap[String, Any]
  val log = Logging(context.system, this)

  override def receive = {
    case SetRequest(key, value) =>
      log.info("received SetRequest - key: {}; value: {}", key, value)
      map += key -> value
    case o => log.info("received unknown message: {}", o);
  }

}
