package spring.reactive;

import io.vertx.core.Future;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

public class VertxBodyInserters {

	public static <T> BodyInserter<Mono<T>, ReactiveHttpOutputMessage> fromVertxFuture(
			Future<T> future, Class<T> elementClass) {
		Mono<T> publisher = futureToMono(future);
		return BodyInserters.fromPublisher(publisher, elementClass);
	}

	public static <T> Mono<T> futureToMono(Future<T> future) {
		return Mono.create(emitter -> {
			future.setHandler(result -> {
				if (result.succeeded()) {
					System.out.println(result.result());
					emitter.success(result.result());
				} else {
					emitter.error(result.cause());
				}
			});
		});
	}


}
