package book.scala.akkademy

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import book.scala.messages.{DeleteRequest, GetRequest, SetIfNotExists, SetRequest}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.reflect.ClassTag

class SClient(remoteAddress: String) {

	private implicit val timeout = Timeout(2.seconds)
	private implicit val system = ActorSystem("LocalSystem")
	private val remoteDb = system.actorSelection(s"akka.tcp://akkademy@$remoteAddress/user/akkademy-db")

	def set(key: String, value: Any): Future[String] = {
		(remoteDb ? SetRequest(key, value)).mapTo[String]
	}

	def setIfNotExists(key: String, value: Any): Future[String] = {
		(remoteDb ? SetIfNotExists(key, value)).mapTo[String]
	}

	def get[T](key: String)(implicit classTag: ClassTag[T]): Future[T] = {
		(remoteDb ? GetRequest(key)).mapTo[T]
	}

	def delete[T](key: String)(implicit classTag: ClassTag[T]): Future[T] = {
		(remoteDb ? DeleteRequest(key)).mapTo[T]
	}

}