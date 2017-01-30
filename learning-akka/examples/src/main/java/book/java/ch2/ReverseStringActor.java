package book.java.ch2;

import akka.actor.AbstractActor;
import akka.actor.Status;
import akka.japi.pf.ReceiveBuilder;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;

public class ReverseStringActor extends AbstractActor {

	@Override
	public PartialFunction<Object, BoxedUnit> receive() {
		return ReceiveBuilder.
				match(String.class, s -> sender().tell(new Status.Success(reverse(s)), self())).
				matchAny(o -> sender().tell(new Status.Failure(new ClassNotFoundException()), self()))
				.build();
	}

	private static String reverse(String str) {
		StringBuilder sb = new StringBuilder(str.length());
		for (int i = str.length() - 1; i >= 0; i--)
			sb.append(str.charAt(i));
		return sb.toString();
	}

}
