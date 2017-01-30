package book.java.akkademy;

import akka.actor.AbstractActor;
import akka.actor.Status;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import book.kotlin.messages.*;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

import java.util.HashMap;
import java.util.Map;

public class AkkademyDb extends AbstractActor {

	final LoggingAdapter log = Logging.getLogger(context().system(), this);
	final Map<String, Object> map = new HashMap<>();

	@Override
	public PartialFunction<Object, BoxedUnit> receive() {
		return ReceiveBuilder.
				match(SetRequest.class, message -> {
					log.info("Received set request – key: {}; value: {}", message.getKey(), message.getValue());
					map.put(message.getKey(), message.getValue());
					sender().tell(new Status.Success(message.getKey()), self());
				}).
				match(GetRequest.class, message -> {
					log.info("Received get request – key: {}", message.getKey());
					Object value = map.get(message.getKey());
					Object response = (value != null)
							? value
							: new Status.Failure(new KeyNotFoundException(message.getKey()));
					sender().tell(response, self());
				}).
				match(SetIfNotExists.class, message -> {
					log.info("Received SetIfNotExists request – key: {}; value: {}", message.getKey(), message.getValue());
					Object previousValue = map.putIfAbsent(message.getKey(), message.getValue());
					Object response = previousValue == null
							? new Status.Success(message.getKey())
							: new Status.Failure(new KeyAlreadyExistsException(message.getKey()));
					sender().tell(response, self());
				}).
				match(DeleteRequest.class, message -> {
					log.info("Received DeleteRequest – key: {}", message.getKey());
					Object removed = map.remove(message.getKey());
					Object response = removed != null
							? new Status.Success(removed)
							: new Status.Failure(new KeyNotFoundException(message.getKey()));
					sender().tell(response, self());
				}).
				matchAny(o -> {
					log.info("Received unknown message: {}", o);
					sender().tell(new Status.Failure(new ClassNotFoundException(o.getClass().toString())), self());
				})
				.build();
	}

}
