package book.scala.akkademy

import book.scala.messages.{KeyAlreadyExistsException, KeyNotFoundException}
import org.scalatest.{FunSpecLike, Matchers}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._


class SClientIntegrationSpec extends FunSpecLike with Matchers {

	val client = new SClient("127.0.0.1:2552")
	val waitDuration = 10.seconds

	describe("akkademyDbClient") {

		it("should set a value") {
			client.set("123", 123)
			val futureResult = client.get[Int]("123")
			val result = Await.result(futureResult, waitDuration)
			result should equal(123)
		}

		it("should throw exception when no value for key") {
			intercept[KeyNotFoundException] {
				Await.result(client.get("unknown identifier"), waitDuration)
			}
		}

		it("should remove value") {
			val future = client.set("123", 123).flatMap(k => client.delete[Int](k))
			val result = Await.result(future, waitDuration)
			result should equal(123)

			intercept[KeyNotFoundException] {
				Await.result(client.get("123"), waitDuration)
			}
		}

		it("should not set value if exists") {
			intercept[KeyAlreadyExistsException] {
				val future = client.set("123", 123)
					.flatMap(k => client.setIfNotExists(k, "new value"))
				Await.result(future, waitDuration)
			}
		}

	}

}
