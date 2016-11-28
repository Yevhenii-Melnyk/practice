package spring.reactive;

import reactor.core.Cancellation;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.springframework.boot.Banner.Mode.LOG;

public class SampleReactor {

	public static void main(String[] args) throws InterruptedException {
		EmitterProcessor<String> stream = EmitterProcessor.<String>create().connect();
		Flux<String> stringFlux = stream.map(String::toUpperCase)
				.doOnNext(s -> System.out.println("1  " + s));
		stringFlux.doOnNext(s -> System.out.println("2  " + s));
		stringFlux.subscribe();
		stream.onNext("Hello World!");


	}

}
