package book.java.ch1;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import book.kotlin.messages.SetRequest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AkkademyDbTest {

	private ActorSystem system = ActorSystem.create();

	@Test
	public void itShouldPlaceKeyValueFromSetMessageIntoMap() {
		TestActorRef<AkkademyDb> actorRef = TestActorRef.create(system, Props.create(AkkademyDb.class));
		actorRef.tell(new SetRequest("key", "value"), ActorRef.noSender());
		AkkademyDb akkademyDb = actorRef.underlyingActor();
		assertEquals(akkademyDb.map.get("key"), "value");
	}

}
