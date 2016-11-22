package my.server.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.rx.java.ObservableFuture;
import io.vertx.rx.java.RxHelper;

public class VertxTest {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        vertx.deployVerticle(new MyVerticle());
    }

}


class MyVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) {
        vertx.createHttpServer()
                .requestHandler(request -> {
                    System.out.println(request);
                    request.handler(System.out::println);
                    request.endHandler(event -> System.out.println("end"));
                    HttpServerResponse response = request.response();
                    response.setStatusCode(200);
                    response.setChunked(true);

                    response.write("Vertx is alive");
                    response.end();
                })
                .listen(8080);

        Router router = Router.router(vertx);

        // This cookie handler will be called for all routes
        router.route().handler(CookieHandler.create());

        // on every path increment the counter
        router.route().handler(ctx -> {
            Cookie someCookie = ctx.getCookie("visits");
            long visits = 0;
            if (someCookie != null) {
                String cookieValue = someCookie.getValue();
                try {
                    visits = Long.parseLong(cookieValue);
                } catch (NumberFormatException e) {
                    visits = 0l;
                }
            }

            // increment the tracking
            visits++;

            // Add a cookie - this will get written back in the response automatically
            ctx.addCookie(Cookie.cookie("visits", "" + visits));

            ctx.next();
        });

        ObservableFuture<HttpServer> observable = RxHelper.observableFuture();
        observable.subscribe(
                server -> {
                    // Server is listening
                },
                failure -> {
                    // Server could not start
                }
        );

        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }


}

class MyReactiveVerticle extends io.vertx.rxjava.core.AbstractVerticle {

    @Override
    public void start() throws Exception {
        io.vertx.rxjava.core.http.HttpServer server = vertx.createHttpServer();

        server.requestStream().toObservable().subscribe(req -> {
            io.vertx.rxjava.core.http.HttpServerResponse resp = req.response();
            resp.setChunked(true);

            req.toObservable().subscribe(
                    resp::write,
                    err -> {
                    },
                    resp::end
            );
        });
        server.listen(8080);
    }

}