package essentials.actor.scala.chapter2.actor

import akka.actor.Actor
import essentials.actor.scala.chapter2.{MapData, ReduceData, WordCount}


class ReduceActor extends Actor {

  def receive: Receive = {
    case MapData(dataList) => sender ! reduce(dataList)
  }

  def reduce(words: IndexedSeq[WordCount]): ReduceData = ReduceData {
    words.foldLeft(Map.empty[String, Int]) { (index, words) =>
      index + (words.word -> (index.getOrElse(words.word, 1) + 1))
    }
  }

}
