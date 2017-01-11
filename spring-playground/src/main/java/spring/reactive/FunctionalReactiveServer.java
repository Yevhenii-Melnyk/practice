package spring.reactive;

import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.server.HttpServer;

import java.io.IOException;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RouterFunctions.toHttpHandler;

public class FunctionalReactiveServer {

	public static final String HOST = "localhost";
	public static final int PORT = 8080;

	public static void main(String[] args) throws InterruptedException, IOException {
		RouterFunction<ServerResponse> route = route(GET("/hello"), FunctionalReactiveServer::sayHelloHandler);
		HttpHandler httpHandler = toHttpHandler(route);

		ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(httpHandler);
		HttpServer server = HttpServer.create(HOST, PORT);
		server.newHandler(adapter).block();

		System.out.println("Press ENTER to exit.");
		System.in.read();
	}

	public static Mono<ServerResponse> sayHelloHandler(ServerRequest request) {
		return ServerResponse.ok().body(Mono.just("Hello!"), String.class);
	}

}
