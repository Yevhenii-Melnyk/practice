package spring.reactive;

import io.vertx.core.Future;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.springframework.http.HttpStatus.FOUND;

@RestController
public class SampleController {

	@GetMapping("/resp")
	public ResponseEntity<Mono<String>> resp() {
		return ResponseEntity.ok().body(Mono.just("abc"));
	}

	// For functional mapping
	@GetMapping("/resp2")
	public Mono<ServerResponse> resp2() {
		return ServerResponse.ok().body(BodyInserters.fromObject(new CustomEntity("abc")));
	}

	@GetMapping("/mono")
	public Mono<String> getMono() {
		return Mono.just("data");
	}

	@GetMapping("/future")
	public Mono<CustomEntity> getFuture() {
		return VertxBodyInserters.futureToMono(Future.succeededFuture(new CustomEntity("something")));
	}

	@GetMapping("/redirect")
	public ResponseEntity redirect() {
		return ResponseEntity.status(FOUND).location(URI.create("/mono")).build();
	}

}


