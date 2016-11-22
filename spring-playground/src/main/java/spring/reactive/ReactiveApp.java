package spring.reactive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.*;
import org.springframework.web.reactive.result.view.freemarker.FreeMarkerViewResolver;
import reactor.core.publisher.Flux;
//import reactor.ipc.netty.http.HttpServer;

import java.io.IOException;
import java.time.Duration;
import java.util.stream.Stream;

import static org.springframework.web.reactive.function.RequestPredicates.GET;
import static org.springframework.web.reactive.function.RouterFunctions.route;
import static org.springframework.web.reactive.function.RouterFunctions.toHttpHandler;

class Wrapper {
	private String value;

	public String getValue() {
		return value;
	}

	public Wrapper(String value) {
		this.value = value;
	}
}

//@SpringBootApplication
public class ReactiveApp {

	private static final String HOST = "localhost";
	private static final int PORT = 8080;

	public static ServerResponse<Rendering> handle(ServerRequest request) {
		Flux<Wrapper> result = Flux.fromStream(Stream.generate(() -> "azaza")
				.limit(3)).delay(Duration.ofSeconds(1)).map(Wrapper::new).log();
		ServerResponse<Rendering> response = ServerResponse.ok().render("index", result);

		return response;
//		return ServerResponse.ok().body(result, Wrapper.class);
	}

	public static RouterFunction<?> router() {
		return route(GET("/"), ReactiveApp::handle);
	}
//
//	@Bean
//	public HttpServer server() {
//		FreeMarkerViewResolver freeMarkerViewResolver = new FreeMarkerViewResolver("", ".ftl");
//		HandlerStrategies strategies = HandlerStrategies.builder().viewResolver(freeMarkerViewResolver).build();
//		HttpHandler httpHandler = toHttpHandler(router(), strategies);
//
//		ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(httpHandler);
//		HttpServer httpServer = HttpServer.create(HOST, PORT);
//		httpServer.start(adapter);
//		return httpServer;
//	}


	public static void main(String[] args) throws IOException, InterruptedException {
//		ReactiveApp app = new ReactiveApp();
//		RouterFunction<?> router = app.router();
//
//		FreeMarkerViewResolver freeMarkerViewResolver = new FreeMarkerViewResolver("", ".ftl");
//		HandlerStrategies strategies = HandlerStrategies.builder().viewResolver(freeMarkerViewResolver).build();
//		HttpHandler httpHandler = toHttpHandler(router, strategies);


//		ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(httpHandler);
//		HttpServer server = HttpServer.create(HOST, PORT);
//		server.startAndAwait(adapter);
		SpringApplication.run(ReactiveApp.class, args);
	}


}
