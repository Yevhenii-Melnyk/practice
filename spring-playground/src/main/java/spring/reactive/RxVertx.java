package spring.reactive;

import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.RxHelper;
import io.vertx.rxjava.core.http.HttpServer;
import io.vertx.rxjava.core.http.HttpServerResponse;
import io.vertx.rxjava.core.streams.Pump;

public class RxVertx {

	public static void main(String[] args) {

	}
}

class SampleVerticle extends AbstractVerticle {

	@Override
	public void start() throws Exception {
		HttpServer server = vertx.createHttpServer();
		server.requestStream().toObservable().subscribe(req -> {
			HttpServerResponse resp = req.response();
			String contentType = req.getHeader("Content-Type");
			if (contentType != null) {
				resp.putHeader("Content-Type", contentType);
			}
			resp.setChunked(true);

			req.toObservable().subscribe(
					resp::write,
					err -> {},
					resp::end
			);
		});
		server.listen(8080);
	}
}
