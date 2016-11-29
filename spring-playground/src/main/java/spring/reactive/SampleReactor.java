package spring.reactive;

import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

public class SampleReactor {

    public static void main(String[] args) {
        EmitterProcessor<String> stream = EmitterProcessor.<String>create().connect();
        Flux<String> flux = stream
                .doOnNext(s -> System.out.println("1 " + s));
        flux .doOnNext(s -> System.out.println("2 " + s));
        flux.subscribe();
        stream.onNext("Hello");
    }

}
