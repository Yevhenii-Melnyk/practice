package my.server;

import java.util.concurrent.CompletableFuture;

public class AsyncTest {

    public static void main(String[] args) throws InterruptedException {
        CompletableFuture<String> stage = CompletableFuture.supplyAsync(() -> {
            System.out.println("Hello");
            return "World!";
        });
        System.out.println("Waiting start");
        Thread.sleep(1000);
        System.out.println("Waiting end");
        stage.thenAccept(System.out::println);
    }

}
