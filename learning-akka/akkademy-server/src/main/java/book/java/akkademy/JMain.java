package book.java.akkademy;

import akka.actor.ActorSystem;
import akka.actor.Props;

public class JMain {

	public static void main(String[] args) {
		ActorSystem system = ActorSystem.create("akkademy");
		system.actorOf(Props.create(AkkademyDb.class), "akkademy-db");
	}

}
