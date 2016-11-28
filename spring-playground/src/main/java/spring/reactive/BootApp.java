package spring.reactive;

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.net.SocketAddress;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CookieHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.reactivestreams.Publisher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.AbstractEmbeddedReactiveHttpServer;
import org.springframework.boot.context.embedded.EmbeddedReactiveHttpServer;
import org.springframework.boot.context.embedded.EmbeddedReactiveHttpServerCustomizer;
import org.springframework.boot.context.embedded.ReactiveHttpServerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.*;
import org.springframework.http.server.reactive.AbstractServerHttpRequest;
import org.springframework.http.server.reactive.AbstractServerHttpResponse;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.HttpHandlerAdapterSupport;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static io.vertx.core.http.HttpHeaders.COOKIE;
import static io.vertx.core.http.HttpHeaders.SET_COOKIE;
import static java.util.function.Function.identity;

@SpringBootApplication
public class BootApp {

	public static void main(String[] args) {
		SpringApplication.run(BootApp.class);
	}

	//	@Bean
	public VertxEmbeddedHttpServerFactory vertxEmbeddedHttpServerFactory() {
		return new VertxEmbeddedHttpServerFactory();
	}

}

@Component
class BookMethodArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return Book.class.isAssignableFrom(parameter.getParameterType());
	}

	@Override
	public Mono<Object> resolveArgument(MethodParameter parameter, BindingContext bindingContext, ServerWebExchange exchange) {
		return Mono.just(new Book(exchange.getRequest().getQueryParams().getFirst("name")));
	}

}

class Book {
	private String name;
	private Integer id;

	public Book() {
	}

	public Book(String name) {
		this.name = name;
	}

	public Book(Integer id, String name) {
		this.name = name;
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public Integer getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Book{" +
				"name='" + name + '\'' +
				", id=" + id +
				'}';
	}
}

@Controller
class BootController {

	@GetMapping("/")
	public
	@ResponseBody
	String get() {
		System.out.println("IN GET METHOD");
		return "Hello reactive!";
	}

	@GetMapping("/p/{code}")
	public
	@ResponseBody
	String path(@PathVariable String code, @RequestParam Integer value) {
		System.out.println("IN GET METHOD");
		return code + "::" + value;
	}

	@GetMapping("/book")
	public
	@ResponseBody
	Book bookGet(Book book) {
		System.out.println(book);
		return book;
	}

	@PostMapping(path = "/b", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public
	@ResponseBody
	Book book(@RequestBody Book book) {
		System.out.println(book);
		return book;
	}
}

class VertxEmbeddedHttpServerFactory implements ReactiveHttpServerFactory {

	@Override
	public EmbeddedReactiveHttpServer getReactiveHttpServer(
			HttpHandler httpHandler,
			EmbeddedReactiveHttpServerCustomizer... embeddedReactiveHttpServerCustomizers) {
		VertxEmbeddedReactiveHttpServer server = new VertxEmbeddedReactiveHttpServer();
		server.setHandler(httpHandler);

		try {
			server.afterPropertiesSet();
			return server;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

class VertxEmbeddedReactiveHttpServer extends AbstractEmbeddedReactiveHttpServer implements EmbeddedReactiveHttpServer {

	private Vertx vertx;

	private VertxHttpHandlerAdapter handler;

	private AtomicBoolean running = new AtomicBoolean();

	@Override
	public void afterPropertiesSet() throws Exception {
		vertx = Vertx.vertx();
		handler = new VertxHttpHandlerAdapter(getHttpHandler());
	}

	@Override
	public void start() {
		if (!running.get()) {
			vertx.deployVerticle(new ReactiveVerticle(), res -> {
				if (res.succeeded()) {
					running.set(true);
				}
			});
		}
	}

	@Override
	public void stop() {
		if (running.get()) {
			vertx.close(res -> {
				if (res.succeeded()) {
					running.set(false);
				}
			});
		}
	}

	@Override
	public boolean isRunning() {
		return running.get();
	}

	private class ReactiveVerticle extends AbstractVerticle {
		@Override
		public void start() throws Exception {
			HttpServerOptions httpServerOptions = new HttpServerOptions();
			httpServerOptions.setPort(getPort());
			if (getAddress() != null) {
				httpServerOptions.setHost(getAddress().getHostAddress());
			}
			vertx.createHttpServer(httpServerOptions).requestHandler(request -> {
				System.out.println("GOT REQUEST");
				handler.apply(request).subscribe();
			}).listen();
		}
	}
}

class VertxHttpHandlerAdapter extends HttpHandlerAdapterSupport implements Function<HttpServerRequest, Mono<Void>> {

	public VertxHttpHandlerAdapter(HttpHandler httpHandler) {
		super(httpHandler);
	}

	@Override
	public Mono<Void> apply(HttpServerRequest serverRequest) {
		NettyDataBufferFactory bufferFactory = new NettyDataBufferFactory(ByteBufAllocator.DEFAULT);
		VertxServerHttpRequest request = new VertxServerHttpRequest(serverRequest, bufferFactory);
		VertxServerHttpResponse response = new VertxServerHttpResponse(serverRequest.response(), bufferFactory);
		return getHttpHandler().handle(request, response)
				.otherwise(ex -> {
					logger.error("Could not complete request", ex);
					serverRequest.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
					return Mono.empty();
				})
				.doOnSuccess(v -> logger.debug("Successfully completed request"));
	}

}

class VertxServerHttpRequest extends AbstractServerHttpRequest {

	private final NettyDataBufferFactory bufferFactory;

	private final HttpServerRequest request;

	public VertxServerHttpRequest(HttpServerRequest request, NettyDataBufferFactory bufferFactory) {
		super(initUri(request), initHeaders(request));
		Assert.notNull(request, "'HttpServerRequest' must not be null.");
		this.request = request;
		this.bufferFactory = bufferFactory;
	}

	private static URI initUri(HttpServerRequest request) {
		Assert.notNull(request, "Vertx 'httpServerRequest' must not be null");
		try {
			URI uri = new URI(request.uri());
			SocketAddress remoteAddress = request.remoteAddress();
			return new URI(
					uri.getScheme(),
					uri.getUserInfo(),
					(remoteAddress != null ? remoteAddress.host() : null),
					(remoteAddress != null ? remoteAddress.port() : -1),
					uri.getPath(),
					uri.getQuery(),
					uri.getFragment());
		} catch (URISyntaxException ex) {
			throw new IllegalStateException("Could not get URI: " + ex.getMessage(), ex);
		}
	}

	private static HttpHeaders initHeaders(HttpServerRequest request) {
		Assert.notNull(request, "Vertx 'httpServerRequest' must not be null");
		HttpHeaders headers = new HttpHeaders();
		for (String name : request.headers().names()) {
			headers.put(name, request.headers().getAll(name));
		}
		return headers;
	}

	@Override
	protected MultiValueMap<String, HttpCookie> initCookies() {
		System.out.println("INIT COOKIES");
		String cookieHeader = request.headers().get(COOKIE);
		MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
		if (cookieHeader != null) {
			Set<io.netty.handler.codec.http.cookie.Cookie> nettyCookies = ServerCookieDecoder.STRICT.decode(cookieHeader);
			for (io.netty.handler.codec.http.cookie.Cookie cookie : nettyCookies) {
				HttpCookie httpCookie = new HttpCookie(cookie.name(), cookie.value());
				cookies.add(cookie.name(), httpCookie);
			}
		}
		System.out.println("COOKIES ARE:");
		System.out.println(cookies);
		return cookies;
	}

	@Override
	public HttpMethod getMethod() {
		return HttpMethod.valueOf(request.method().name());
	}

	@Override
	public Flux<DataBuffer> getBody() {
		EmitterProcessor<Buffer> stream = EmitterProcessor.<Buffer>create().connect();
		request.handler(stream::onNext);
		request.endHandler(e -> stream.onComplete());
		return stream.map(buffer -> bufferFactory.wrap(buffer.getByteBuf()));
	}
}

class VertxServerHttpResponse extends AbstractServerHttpResponse {

	private static final Buffer END_SIGNAL = Buffer.buffer(Unpooled.buffer(0, 0));

	private final HttpServerResponse response;

	public VertxServerHttpResponse(HttpServerResponse response, DataBufferFactory dataBufferFactory) {
		super(dataBufferFactory);
		Assert.notNull(response, "'HttpServerResponse' must not be null.");
		this.response = response;
	}

	@Override
	protected Mono<Void> doCommit() {
		return doCommit(() -> {
			response.end();
			return Mono.empty();
		});
	}

	@Override
	protected Mono<Void> writeWithInternal(Publisher<? extends DataBuffer> publisher) {
		return toBuffers(publisher)
				.doOnNext(response::write)
				.doOnNext(e -> System.out.println("Writing"))
				.doOnComplete(response::end)
				.doOnComplete(() -> System.out.println("Completed writing"))
				.then();
	}

	@Override
	protected Mono<Void> writeAndFlushWithInternal(Publisher<? extends Publisher<? extends DataBuffer>> publisher) {
		System.out.println("INTERNAL");
		return writeWithInternal(Flux.from(publisher).flatMap(identity()));

	}

	@Override
	protected void applyStatusCode() {
		HttpStatus statusCode = getStatusCode();
		if (statusCode != null) {
			response.setStatusCode(statusCode.value());
		}
	}

	@Override
	protected void applyHeaders() {
		HttpHeaders headers = getHeaders();
		if (!headers.containsKey(HttpHeaders.CONTENT_LENGTH)) {
			response.setChunked(true);
		}
		for (String name : headers.keySet()) {
			for (String value : headers.get(name)) {
				response.putHeader(name, value);
			}
		}
	}

	@Override
	protected void applyCookies() {
		for (String name : getCookies().keySet()) {
			for (ResponseCookie httpCookie : getCookies().get(name)) {
				io.netty.handler.codec.http.cookie.Cookie nettyCookie = new DefaultCookie(httpCookie.getName(), httpCookie.getValue());
				if (!httpCookie.getMaxAge().isNegative()) {
					nettyCookie.setMaxAge(httpCookie.getMaxAge().getSeconds());
				}
				httpCookie.getDomain().ifPresent(nettyCookie::setDomain);
				httpCookie.getPath().ifPresent(nettyCookie::setPath);
				nettyCookie.setSecure(httpCookie.isSecure());
				nettyCookie.setHttpOnly(httpCookie.isHttpOnly());
				response.headers().add(SET_COOKIE, ServerCookieEncoder.STRICT.encode(nettyCookie));
			}
		}
	}

	private static Flux<Buffer> toBuffers(Publisher<? extends DataBuffer> dataBuffers) {
		return Flux.from(dataBuffers).map(b -> Buffer.buffer(NettyDataBufferFactory.toByteBuf(b)));
	}
}
