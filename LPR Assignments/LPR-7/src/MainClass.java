import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class MainClass {

	public static void main(String[] args) {
		ServerSocketChannel serverChannel;
		Selector selector;
		try {
			serverChannel = ServerSocketChannel.open();
			serverChannel.configureBlocking(false);
			serverChannel.socket().bind(new InetSocketAddress(8000));
			System.out.println("Server attivo su porta 8000");

			selector = Selector.open();
			serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return;
		}

		while (true) {
			try {
				selector.select();
			} catch (IOException e) {
				System.err.println(e.getMessage());
				break;
			}

			Set<SelectionKey> readyKeys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = readyKeys.iterator();

			while (iterator.hasNext()) {
				SelectionKey key = iterator.next();
				iterator.remove();
				try {
					if (key.isAcceptable()) {
						ServerSocketChannel server = (ServerSocketChannel) key.channel();
						SocketChannel client = server.accept();
						System.out.println("Accepted connection from " + client);
						client.configureBlocking(false);
						SelectionKey key2 = client.register(selector, SelectionKey.OP_WRITE);
						ByteBuffer output = ByteBuffer.allocate(4);
						output.putInt(0);
						output.flip();
						key2.attach(output);
					} else if (key.isWritable()) {
						SocketChannel client = (SocketChannel) key.channel();
						ByteBuffer output = (ByteBuffer) key.attachment();
						if (!output.hasRemaining()) {
							output.rewind();
							int value = output.getInt();
							output.clear();
							output.putInt(value + 1);
							output.flip();
						}
						client.write(output);
					}
				} catch (IOException e) {
					System.err.println(e.getMessage());
					key.cancel();
					try {
						key.channel().close();
					} catch (IOException ex) {
					}
				}
			}
		}
	}
}
