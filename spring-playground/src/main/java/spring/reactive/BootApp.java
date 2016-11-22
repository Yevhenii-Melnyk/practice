package spring.reactive;

import io.vertx.rxjava.core.buffer.Buffer;
import io.vertx.rxjava.core.http.HttpServerRequest;
import io.vertx.rxjava.core.http.HttpServerResponse;
import io.vertx.rxjava.core.net.SocketAddress;
import io.vertx.rxjava.ext.web.Cookie;
import io.vertx.ext.web.impl.CookieImpl;
import io.vertx.rxjava.ext.web.RoutingContext;
import org.reactivestreams.Publisher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedReactiveHttpServer;
import org.springframework.boot.context.embedded.EmbeddedReactiveHttpServerCustomizer;
import org.springframework.boot.context.embedded.ReactiveHttpServerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.NettyDataBuffer;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.AbstractServerHttpRequest;
import org.springframework.http.server.reactive.AbstractServerHttpResponse;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.HttpHandlerAdapterSupport;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rx.Observable;
import rx.RxReactiveStreams;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.function.IntPredicate;

@SpringBootApplication
public class BootApp {

    public static void main(String[] args) {
        SpringApplication.run(BootApp.class);
    }

}

@Controller
class BootController {

    @GetMapping("/")
    public
    @ResponseBody
    String get() {
        return "Hello reactive!";
    }

}

class HttpServerFactory implements ReactiveHttpServerFactory {

    @Override
    public EmbeddedReactiveHttpServer getReactiveHttpServer(
            HttpHandler httpHandler,
            EmbeddedReactiveHttpServerCustomizer... embeddedReactiveHttpServerCustomizers) {
        return null;
    }
}

class VertxHttpHandlerAdapter extends HttpHandlerAdapterSupport {

    public VertxHttpHandlerAdapter(HttpHandler httpHandler) {
        super(httpHandler);
    }

    public VertxHttpHandlerAdapter(Map<String, HttpHandler> handlerMap) {
        super(handlerMap);
    }

}

class VertxServerHttpRequest extends AbstractServerHttpRequest {

    private final NettyDataBufferFactory bufferFactory;

    private final RoutingContext routingContext;

    private final HttpServerRequest request;

    public VertxServerHttpRequest(RoutingContext routingContext, NettyDataBufferFactory bufferFactory) {
        super(initUri(routingContext.request()), initHeaders(routingContext.request()));
        this.routingContext = routingContext;
        this.request = routingContext.request();
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
        HttpHeaders headers = new HttpHeaders();
        for (String name : request.headers().names()) {
            headers.put(name, request.headers().getAll(name));
        }
        return headers;
    }

    @Override
    protected MultiValueMap<String, HttpCookie> initCookies() {
        MultiValueMap<String, HttpCookie> cookies = new LinkedMultiValueMap<>();
        for (Cookie cookie : routingContext.cookies()) {
            HttpCookie httpCookie = new HttpCookie(cookie.getName(), cookie.getValue());
            cookies.add(cookie.getName(), httpCookie);
        }
        return cookies;
    }

    @Override
    public HttpMethod getMethod() {
        return HttpMethod.valueOf(request.method().name());
    }

    @Override
    public Flux<DataBuffer> getBody() {
        Observable<NettyDataBuffer> body = request.toObservable()
                .map(buffer ->
                        bufferFactory.wrap(
                                ((io.vertx.core.buffer.Buffer) buffer.getDelegate()).getByteBuf())
                );
        return Flux.from(RxReactiveStreams.toPublisher(body));
    }
}


class VertxServerHttpResponse extends AbstractServerHttpResponse {

    private final RoutingContext routingContext;

    private final HttpServerResponse response;

    public VertxServerHttpResponse(DataBufferFactory dataBufferFactory, RoutingContext routingContext) {
        super(dataBufferFactory);
        this.routingContext = routingContext;
        this.response = routingContext.response();
    }

    @Override
    protected Mono<Void> writeWithInternal(Publisher<DataBuffer> body) {
Flux.from(body).subscribe();
        return null;
    }

    @Override
    protected Mono<Void> writeAndFlushWithInternal(Publisher<Publisher<DataBuffer>> body) {
        return null;
    }

    @Override
    protected void applyStatusCode() {
        response.setStatusCode(getStatusCode().value());
    }

    @Override
    protected void applyHeaders() {
        HttpHeaders headers = getHeaders();
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
                Cookie cookie = Cookie.cookie(name, httpCookie.getValue());
                if (!httpCookie.getMaxAge().isNegative()) {
                    cookie.setMaxAge(httpCookie.getMaxAge().getSeconds());
                }
                httpCookie.getDomain().ifPresent(cookie::setDomain);
                httpCookie.getPath().ifPresent(cookie::setPath);
                cookie.setSecure(httpCookie.isSecure());
                cookie.setHttpOnly(httpCookie.isHttpOnly());
                routingContext.addCookie(cookie);
            }
        }
    }
}

class VertxDataBuffer implements DataBuffer {

    private final Buffer buffer;

    public VertxDataBuffer(Buffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public DataBufferFactory factory() {
        return null;
    }

    @Override
    public int indexOf(IntPredicate predicate, int fromIndex) {

        return 0;
    }

    @Override
    public int lastIndexOf(IntPredicate predicate, int fromIndex) {
        return 0;
    }

    @Override
    public int readableByteCount() {
        return 0;
    }

    @Override
    public byte read() {
        return 0;
    }

    @Override
    public DataBuffer read(byte[] destination) {
        return null;
    }

    @Override
    public DataBuffer read(byte[] destination, int offset, int length) {
        return null;
    }

    @Override
    public DataBuffer write(byte b) {
        return null;
    }

    @Override
    public DataBuffer write(byte[] source) {
        return null;
    }

    @Override
    public DataBuffer write(byte[] source, int offset, int length) {
        return null;
    }

    @Override
    public DataBuffer write(DataBuffer... buffers) {
        return null;
    }

    @Override
    public DataBuffer write(ByteBuffer... buffers) {
        return null;
    }

    @Override
    public DataBuffer slice(int index, int length) {
        return null;
    }

    @Override
    public ByteBuffer asByteBuffer() {
        return null;
    }

    @Override
    public InputStream asInputStream() {
        return null;
    }

    @Override
    public OutputStream asOutputStream() {
        return null;
    }
}