package my.server.blocking;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class BlockingServer {

	public static void main(String[] args) throws IOException {
		ServerSocket ss = new ServerSocket(8080);
		while (!Thread.interrupted())
			new Thread(new Handler(ss.accept())).start();
	}

	static class Handler implements Runnable {
		final Socket socket;

		Handler(Socket s) {
			socket = s;
		}

		public void run() {
			try {
				byte[] input = new byte[1000];
				socket.getInputStream().read(input);
				byte[] output = process(input);
				socket.getOutputStream().write(output);
				socket.close();
			} catch (IOException ex) { /* ... */ }
		}

		private byte[] process(byte[] cmd) {
			return "Hello world".getBytes();
		}
	}

}
