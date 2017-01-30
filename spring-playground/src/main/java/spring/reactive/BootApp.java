package spring.reactive;

import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.streams.Pump;
import io.vertx.ext.reactivestreams.ReactiveReadStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.AbstractEmbeddedReactiveHttpServer;
import org.springframework.boot.context.embedded.EmbeddedReactiveHttpServer;
import org.springframework.boot.context.embedded.EmbeddedReactiveHttpServerCustomizer;
import org.springframework.boot.context.embedded.ReactiveHttpServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.*;
import org.springframework.http.server.reactive.AbstractServerHttpRequest;
import org.springframework.http.server.reactive.AbstractServerHttpResponse;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.HttpHandlerAdapterSupport;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

@RestController
class BootController {

	@Autowired
	private ItemRepository itemRepository;

	@GetMapping("/")
	public
	@ResponseBody
	String get() {
//		System.out.println("IN GET METHOD");
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

	@RequestMapping("/item")
	public Mono<Item> item(@PathVariable("repetitions") int repetitions) {
		return this.itemRepository.findAllItems(Math.max(repetitions, 1)).next();
	}

	@RequestMapping("/items/{repetitions}")
	public Flux<Item> items(@PathVariable("repetitions") int repetitions) {
		return this.itemRepository.findAllItems(Math.max(repetitions, 1));
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
		VertxOptions vertxOptions = new VertxOptions()
				.setMaxEventLoopExecuteTime(Long.MAX_VALUE);
		vertx = Vertx.vertx(vertxOptions);
		handler = new VertxHttpHandlerAdapter(getHttpHandler());
	}

	@Override
	public void start() {
		if (!running.get()) {
			DeploymentOptions options = new DeploymentOptions().setWorker(true).setMaxWorkerExecuteTime(Long.MAX_VALUE);
//			vertx.deployVerticle(new ReactiveVerticle(), options, res -> {
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

		protected final Log logger = LogFactory.getLog(getClass());

		@Override
		public void start() throws Exception {
			logger.debug("START");
			HttpServerOptions httpServerOptions = new HttpServerOptions();
			httpServerOptions.setPort(getPort());
			if (getAddress() != null) {
				httpServerOptions.setHost(getAddress().getHostAddress());
			}
			vertx.createHttpServer(httpServerOptions).requestHandler(request -> {
				logger.debug(Thread.currentThread().getName());
				logger.debug("BEFORE SUBSCRIBE");
				handler.apply(request).subscribe();
				logger.debug("AFTER SUBSCRIBE");
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
				.doOnSubscribe(s -> logger.debug("OUTER SUBSCRIBE"))
				.otherwise(ex -> {
					logger.error("Could not complete request", ex);
					serverRequest.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
					serverRequest.response().end();
					return Mono.empty();
				})
				.doOnSuccess(v -> {
					logger.debug("DO ON SUCCESS");
					if (!serverRequest.response().ended()) {
						serverRequest.response().end();
					}
					logger.debug("Successfully completed request");
				});
	}

}

class VertxServerHttpRequest extends AbstractServerHttpRequest {

	protected final Log logger = LogFactory.getLog(getClass());
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
		logger.debug("initCookies");
		String cookieHeader = request.headers().get(COOKIE);
		MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
		if (cookieHeader != null) {
			Set<Cookie> nettyCookies = ServerCookieDecoder.STRICT.decode(cookieHeader);
			for (Cookie cookie : nettyCookies) {
				HttpCookie httpCookie = new HttpCookie(cookie.name(), cookie.value());
				cookies.add(cookie.name(), httpCookie);
			}
		}
		return cookies;
	}

	@Override
	public HttpMethod getMethod() {
		return HttpMethod.valueOf(request.method().name());
	}

	@Override
	public Flux<DataBuffer> getBody() {
		logger.debug("getBody");
		EmitterProcessor<Buffer> stream = EmitterProcessor.<Buffer>create().connect();
		request.handler(stream::onNext);
		request.endHandler(e -> stream.onComplete());
		return stream.map(buffer -> bufferFactory.wrap(buffer.getByteBuf()));
	}
}

class VertxServerHttpResponse extends AbstractServerHttpResponse {

	protected final Log logger = LogFactory.getLog(getClass());
	private final HttpServerResponse response;

	public VertxServerHttpResponse(HttpServerResponse response, DataBufferFactory dataBufferFactory) {
		super(dataBufferFactory);
		Assert.notNull(response, "'HttpServerResponse' must not be null.");
		this.response = response;
	}

	@Override
	protected Mono<Void> writeWithInternal(Publisher<? extends DataBuffer> publisher) {
		logger.debug("WRITE WITH INTERNAL");
		Flux<Buffer> body = toBuffers(publisher);
		ReactiveReadStream<Buffer> rrs = ReactiveReadStream.readStream();
		Pump pump = Pump.pump(rrs, response);
		Flux<Void> transform = body.transform(original -> {
			original.subscribe(rrs);
			logger.debug("TRANSFORM");
			return Flux.<Void>empty()
					.doOnComplete(() -> logger.debug("ENDED VOID"));
		});
		return transform.then()
				.doOnSubscribe(s -> {
					logger.debug("PUMP START");
					pump.start();
					logger.debug("PUMP END");
				});
	}



	private static Flux<Buffer> toBuffers(Publisher<? extends DataBuffer> dataBuffers) {
		return Flux.from(dataBuffers)
				.map(NettyDataBufferFactory::toByteBuf)
				.map(Buffer::buffer);
	}

	@Override
	protected Mono<Void> writeAndFlushWithInternal(Publisher<? extends Publisher<? extends DataBuffer>> publisher) {
		// Vertx response flushes data when the internal buffer is full, so just flatten the stream
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
				Cookie nettyCookie = new DefaultCookie(name, httpCookie.getValue());
				if (!httpCookie.getMaxAge().isNegative()) {
					nettyCookie.setMaxAge(httpCookie.getMaxAge().getSeconds());
				}
				httpCookie.getDomain().ifPresent(nettyCookie::setDomain);
				httpCookie.getPath().ifPresent(nettyCookie::setPath);
				nettyCookie.setSecure(httpCookie.isSecure());
				nettyCookie.setHttpOnly(httpCookie.isHttpOnly());
				response.headers()
						.add(SET_COOKIE, ServerCookieEncoder.STRICT.encode(nettyCookie));
			}
		}
	}

}
