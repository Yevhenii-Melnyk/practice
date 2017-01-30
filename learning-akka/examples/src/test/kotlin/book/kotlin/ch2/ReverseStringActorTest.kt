package book.kotlin.ch2

import akka.actor.ActorSystem
import book.kotlin.util.Props
import book.kotlin.util.ask
import book.kotlin.util.block
import book.kotlin.util.sequence
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*
import java.util.concurrent.ExecutionException

class ReverseStringActorTest {

	private val system = ActorSystem.create()

	private val reverseActor = system.actorOf(Props<ReverseStringActor>())

	@Test
	fun `should reverse string`() {
		val future = reverseActor.ask<String>("123", 1000)

		assertEquals("321", future.block(1000))
	}

	@Test(expected = ExecutionException::class)
	fun `should fail when unknown message sent`() {
		val future = reverseActor.ask<Any>(123, 1000)

		future.block(1000)
	}

	@Test
	fun `should reverse multiple messages`() {
		val futures = listOf("123", "abc", "aaa").map { reverseActor.ask<String>(it, 1000) }
		val result = futures.sequence<String>()

		assertEquals(Arrays.asList("321", "cba", "aaa"), result.block(1000))
	}

}


