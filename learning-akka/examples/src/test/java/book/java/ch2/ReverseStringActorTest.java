package book.java.ch2;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import book.kotlin.util.ActorExtensions;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static book.kotlin.util.ActorExtensions.*;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

public class ReverseStringActorTest {

	private ActorSystem system = ActorSystem.create();

	private ActorRef reverseActor = system.actorOf(Props.create(ReverseStringActor.class));

	@Test
	public void shouldReverseString() {
		CompletableFuture<String> future = ask(reverseActor, "123", 1000);

		assertEquals("321", block(future, 1000));
	}

	@Test(expected = ExecutionException.class)
	public void shouldFailWhenUnknownMessageSent() {
		CompletableFuture<Object> future = ask(reverseActor, 123, 1000);

		block(future, 1000);
	}

	@Test
	public void shouldRevertMultipleMessages() {
		List<CompletableFuture<String>> futures = Stream.of("123", "abc", "aaa")
				.map(s -> ActorExtensions.<String>ask(reverseActor, s, 1000))
				.collect(toList());
		CompletableFuture<List<String>> result = sequence(futures);
		System.out.println("RESULT " + result);
		assertEquals(Arrays.asList("321", "cba", "aaa"), block(result, 1000));
	}

}
