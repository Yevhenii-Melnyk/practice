package spring.reactive;

import reactor.core.Cancellation;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.boot.Banner.Mode.LOG;

public class SampleReactor {

    public static void main(String[] args) {
        // A Flux is a data publisher
        EmitterProcessor<String> stream = EmitterProcessor.<String>create().connect();

        // Transform values passing through the Flux, observe and capture the result once.
        Flux<String> stringFlux = stream.map(String::toUpperCase)
                .doOnNext(s -> System.out.println("1  " + s));
        stringFlux.doOnNext(s -> System.out.println("2  " + s));
        Cancellation promise = stringFlux
                .subscribe();

        // Publish a value
        stream.onNext("Hello World!");

    }

}
