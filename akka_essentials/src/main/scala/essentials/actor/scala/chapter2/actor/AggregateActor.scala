package essentials.actor.scala.chapter2.actor

import akka.actor.Actor
import essentials.actor.scala.chapter2.{ReduceData, Result}

import scala.collection.mutable

class AggregateActor extends Actor {

  val finalReducedMap = new mutable.HashMap[String, Int]

  def receive: Receive = {
    case ReduceData(reduceDataMap) => aggregateInMemoryReduce(reduceDataMap)
    case Result => sender ! finalReducedMap.toString()
  }

  def aggregateInMemoryReduce(reducedList: Map[String, Int]): Unit = {
    for ((key, value) <- reducedList) {
      finalReducedMap += (key -> (value + finalReducedMap.getOrElse(key, 0)))
    }
  }

}
