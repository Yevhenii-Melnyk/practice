package book.java.akkademy;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import book.kotlin.messages.SetRequest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AkkademyDbTest {

	private static ActorSystem system;

	@BeforeClass
	public static void setUp() {
		system = ActorSystem.create();
	}

	@AfterClass
	public static void shutDown(){
		system.shutdown();
	}

	@Test
	public void itShouldPlaceKeyValueFromSetMessageIntoMap() {
		TestActorRef<AkkademyDb> actorRef = TestActorRef.create(system, Props.create(AkkademyDb.class));
		actorRef.tell(new SetRequest("key", "value"), ActorRef.noSender());
		AkkademyDb akkademyDb = actorRef.underlyingActor();
		assertEquals(akkademyDb.map.get("key"), "value");
	}

}
