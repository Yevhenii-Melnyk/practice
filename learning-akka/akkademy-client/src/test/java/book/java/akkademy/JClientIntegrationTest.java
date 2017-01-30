package book.java.akkademy;

import book.kotlin.messages.KeyAlreadyExistsException;
import book.kotlin.messages.KeyNotFoundException;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

public class JClientIntegrationTest {

	private static JClient client = new JClient("127.0.0.1:2552");

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void shouldSetRecord() throws Exception {
		client.set("123", 123);
		Integer result = client.<Integer>get("123").get();

		assertEquals(Integer.valueOf(123), result);
	}

	@Test
	public void shouldThrowExceptionWhenNoValueForKey() throws Exception {
		expectException(KeyNotFoundException.class);

		client.get("unknown identifier").get();
	}

	@Test
	public void shouldRemoveValue() throws Exception {
		Integer removedValue = client.set("key", 12345)
				.thenCompose(k -> client.<Integer>delete(k))
				.get();
		assertEquals(Integer.valueOf(12345), removedValue);

		expectException(KeyNotFoundException.class);
		client.get("key").get();
	}

	@Test
	public void shouldNotSetValueIfExists() throws Exception {
		expectException(KeyAlreadyExistsException.class);

		client.set("123", 123)
				.thenCompose(k -> client.setIfNotExists(k, "new value"))
				.get();
	}

	private void expectException(Class<? extends Exception> clazz) {
		expectedException.expectCause(IsInstanceOf.instanceOf(clazz));
	}
}
