package my.server.nonblocking;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Reactor {

	final ServerSocketChannel serverSocket;

	private static final int MAX = 2;

	ExecutorService executorService = Executors.newFixedThreadPool(MAX);

	Reactor(int port) throws IOException {
		serverSocket = ServerSocketChannel.open();
		serverSocket.socket().bind(new InetSocketAddress(port));
		serverSocket.configureBlocking(false);
		for (int i = 0; i < MAX; i++) {
			Selector selector = Selector.open();
			SelectionKey selectionKey = serverSocket.register(selector, SelectionKey.OP_ACCEPT);
			selectionKey.attach(new Acceptor(selector));
			executorService.execute(new ReactorSelector(selector));
		}
	}

	private class Acceptor implements Runnable {
		Selector selector;

		public Acceptor(Selector selector) {
			this.selector = selector;
		}

		public void run() {
			try {
				SocketChannel socketChannel = serverSocket.accept();
				if (socketChannel != null)
					new Handler(selector, socketChannel);
			} catch (IOException ex) { /* ... */ }
		}
	}

	public static void main(String[] args) throws IOException {
		new Reactor(8080);
	}
}


class ReactorSelector implements Runnable {

	final Selector selector;

	public ReactorSelector(Selector selector) {
		this.selector = selector;
	}

	public void run() {
		try {
			while (!Thread.interrupted()) {
				selector.select();
				Set<SelectionKey> selected = selector.selectedKeys();
				for (SelectionKey key : selected)
					dispatch(key);
				selected.clear();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private void dispatch(SelectionKey k) {
		Runnable r = (Runnable) (k.attachment());
		if (r != null)
			r.run();
	}
}


final class Handler implements Runnable {
	private static final int SIZE = 1000;

	private final SocketChannel socket;
	private final SelectionKey selectionKey;
	private ByteBuffer input = ByteBuffer.allocate(SIZE);

	private static final int READING = 0;
	private static final int SENDING = 1;

	private int state = READING;

	Handler(Selector selector, SocketChannel socketChannel) throws IOException {
		socket = socketChannel;
		socketChannel.configureBlocking(false);
		// Optionally try first read now
		selectionKey = socket.register(selector, 0);
		selectionKey.attach(this);
		selectionKey.interestOps(SelectionKey.OP_READ);
		selector.wakeup();
	}

	public void run() {
		try {
			if (state == READING) read();
			else if (state == SENDING) send();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	void read() throws IOException {
		socket.read(input);
		state = SENDING;
		selectionKey.interestOps(SelectionKey.OP_WRITE);
	}

	void send() throws IOException {
		ByteBuffer output = ByteBuffer.wrap(("Hello world").getBytes());
		socket.write(output);
		selectionKey.cancel();
		socket.close();
	}

}
