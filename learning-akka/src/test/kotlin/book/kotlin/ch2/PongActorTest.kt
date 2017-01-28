package book.kotlin.ch2

import akka.actor.ActorRef
import akka.actor.ActorSystem
import book.kotlin.util.*
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.subject.SubjectSpek
import org.junit.Assert.assertEquals
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import java.util.concurrent.ExecutionException
import kotlin.test.assertFailsWith

@RunWith(JUnitPlatform::class)
class PongActorTest : SubjectSpek<ActorRef>({
	subject {
		val system = ActorSystem.create()
		system.actorOf(Props<KotlinPongActor>())
	}
	describe("Pong actor") {

		it("should respond with Pong") {
			println(subject)
			val future = subject.ask<String>("Ping")
			assertEquals("Pong", future.block(1000))
		}

		it("should fail on unknown message") {
			println(subject)
			val future = subject.ask<String>("unknown")
			assertFailsWith(ExecutionException::class) {
				future.block(1000)
			}
		}

	}

})



