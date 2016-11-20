package my.server.blocking;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlockingPoolServer {

	public static void main(String[] args) throws IOException {
		ExecutorService executorService = Executors.newFixedThreadPool(100);
		ServerSocket ss = new ServerSocket(8080);
		while (!Thread.interrupted())
			executorService.execute(new BlockingServer.Handler(ss.accept()));
	}


}
