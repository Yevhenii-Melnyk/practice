package book.kotlin.akkademy

import book.kotlin.messages.KeyAlreadyExistsException
import book.kotlin.messages.KeyNotFoundException
import org.hamcrest.core.IsInstanceOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class KClientIntegrationTest {

	companion object {
		private val client = KClient("127.0.0.1:2552")
	}

	@get:Rule
	val expectedException = ExpectedException.none()

	@Test
	fun `should set record`() {
		client.set("123", 123)
		val result = client.get<Int>("123").get()

		assertEquals(123, result)
	}

	@Test
	fun `should throw exception when no value for key`() {
		expectException<KeyNotFoundException>()

		client.get<Any>("unknown identifier").get()
	}

	@Test
	fun `should remove value`() {
		val removedValue = client.set("key", 12345)
				.thenCompose { k -> client.delete<Int>(k) }
				.get()
		assertEquals(Integer.valueOf(12345), removedValue)

		expectException<KeyNotFoundException>()
		client.get<Any>("key").get()
	}

	@Test
	fun `should not set value if exists`() {
		expectException<KeyAlreadyExistsException>()

		client.set("123", 123)
				.thenCompose { k -> client.setIfNotExists(k, "new value") }
				.get()
	}

	inline private fun <reified T : Throwable> expectException() {
		expectedException.expectCause(IsInstanceOf.instanceOf<Throwable>(T::class.java))
	}

}
