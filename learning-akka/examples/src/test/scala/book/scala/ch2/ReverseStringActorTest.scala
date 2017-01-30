package book.scala.ch2

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import org.scalatest.{FunSpecLike, Matchers}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class ReverseStringActorTest extends FunSpecLike with Matchers {

	val system = ActorSystem()
	implicit val timeout = Timeout(1.second)
	val reverseActor = system.actorOf(Props(classOf[ReverseStringActor]))

	describe("Reverse string actor") {
		it("should reverse string") {
			val future = reverseActor ? "123"

			val result = Await.result(future.mapTo[String], 1.second)
			result shouldBe "321"
		}

		it("should fail when unknown message sent") {
			val future = reverseActor ? 123
			intercept[ClassNotFoundException] {
				Await.result(future.mapTo[String], 1.second)
			}
		}

		it("should reverse multiple strings") {
			val listOfFutures = List("123", "abc", "aaa").map(reverseActor ? _).map(_.mapTo[String])
			val futureOfList = Future.sequence(listOfFutures)

			val result = Await.result(futureOfList, 1.second)
			result shouldBe List("321", "cba", "aaa")
		}
	}

}
