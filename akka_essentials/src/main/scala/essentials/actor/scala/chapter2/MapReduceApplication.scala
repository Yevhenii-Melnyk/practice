package essentials.actor.scala.chapter2

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import essentials.actor.scala.chapter2.actor.MasterActor

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await

sealed trait MapReduceMessage

case class WordCount(word: String, count: Int) extends MapReduceMessage

case class MapData(dataList: ArrayBuffer[WordCount]) extends MapReduceMessage

case class ReduceData(reduceDataMap: Map[String, Int]) extends MapReduceMessage

case class Result() extends MapReduceMessage

object MapReduceApplication {

  def main(args: Array[String]) {
    val system = ActorSystem("MapReduceApp")
    val master = system.actorOf(Props[MasterActor], name = "master")
    implicit val timeout = Timeout(1, TimeUnit.SECONDS)
    master ! "The quick brown fox tried to jump over the lazy dog and  fell on the dog "
    master ! "Dog is man's best friend"
    master ! "Dog and Fox belong to the same family"
    Thread.sleep(1000)
    val future = ask(master, Result).mapTo[String]
    val result = Await.result(future, timeout.duration)
    println(result)
    system.terminate()
  }

}