package book.kotlin.akkademy

import akka.actor.ActorSystem
import book.kotlin.util.Props

fun main(args: Array<String>) {
	val system = ActorSystem.create("akkademy");
	system.actorOf(Props<AkkademyDb>(), "akkademy-db");
}
