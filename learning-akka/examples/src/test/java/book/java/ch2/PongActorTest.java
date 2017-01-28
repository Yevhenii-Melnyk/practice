package book.java.ch2;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import org.junit.Test;
import scala.concurrent.Future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static akka.pattern.Patterns.ask;
import static org.junit.Assert.assertEquals;
import static scala.compat.java8.FutureConverters.toJava;

public class PongActorTest {

	ActorSystem system = ActorSystem.create();
	ActorRef actorRef = system.actorOf(Props.create(JavaPongActor.class));

	@Test
	public void shouldReplyToPingWithPong() throws Exception {
		Future sFuture = ask(actorRef, "Ping", 1000);
		CompletionStage<String> cs = toJava(sFuture);
		CompletableFuture<String> jFuture = cs.toCompletableFuture();
		assertEquals("Pong", jFuture.get(1000, TimeUnit.MILLISECONDS));
	}

	@Test(expected = ExecutionException.class)
	public void shouldReplyToUnknownMessageWithFailure() throws Exception {
		Future sFuture = ask(actorRef, "unknown", 1000);
		CompletionStage<String> cs = toJava(sFuture);
		CompletableFuture<String> jFuture = cs.toCompletableFuture();
		jFuture.get(1000, TimeUnit.MILLISECONDS);
	}

}