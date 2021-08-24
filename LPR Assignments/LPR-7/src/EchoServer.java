/*
 * Leonardo Vona
 * 545042
 */

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/*
 * Multiplexed server
 */
public class EchoServer {
	//EchoServer [PORT]
	//PORT (opzionale): intero che indica la porta su cui accettare le connessioni
	
	public static final int DEFAULT_PORT = 1919; // porta di default

	public static void main(String[] args) {
		int port; // porta scelta dall'utente

		try {
			port = Integer.parseInt(args[0]); // se passata come argomento del programma, imposta la porta definita dall'utente
		} catch (RuntimeException e) {
			port = DEFAULT_PORT; // porta non definita, viene impostata quella di default
		}

		System.out.println("Listening for connections on port " + port);

		ServerSocketChannel serverChannel;
		Selector selector;
		try {
			serverChannel = ServerSocketChannel.open();
			serverChannel.socket().bind(new InetSocketAddress(port)); // bind del processo server alla porta definita
			serverChannel.configureBlocking(false); // imposta che non sia bloccante

			selector = Selector.open();
			serverChannel.register(selector, SelectionKey.OP_ACCEPT); // registra l'evento di accettazione di una
																		// connessione
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

			Set<SelectionKey> readyKeys = selector.selectedKeys(); // recupera le chiavi da gestire
			Iterator<SelectionKey> iterator = readyKeys.iterator();

			while (iterator.hasNext()) {
				SelectionKey key = iterator.next(); // recupera la chiave
				iterator.remove(); // rimuove la chiave dall'iteratore
				try {
					if (key.isAcceptable()) { // accettazione della connessione
						ServerSocketChannel server = (ServerSocketChannel) key.channel();
						SocketChannel client = server.accept(); // accetta la connessione
						System.out.println("Accepted connection from " + client);
						client.configureBlocking(false);
						SelectionKey key2 = client.register(selector, SelectionKey.OP_READ); // registra la lettura da parte del client
						ByteBuffer buffer = ByteBuffer.allocate(1024); // buffer relativo al client
						key2.attach(buffer);

					}

					if (key.isReadable()) { // riceve qualcosa da un client
						SocketChannel client = (SocketChannel) key.channel();
						ByteBuffer buffer = (ByteBuffer) key.attachment();
						client.read(buffer); // recupera la richiesta del client
						key.interestOps(SelectionKey.OP_WRITE); // imposta una richiesta di scrittura verso il client

					}

					if (key.isWritable()) { // deve inviare qualcosa ad un client
						SocketChannel client = (SocketChannel) key.channel();
						ByteBuffer buffer = (ByteBuffer) key.attachment();
						buffer.flip(); // imposta il buffer per la lettura
						client.write(buffer); // invia al client i dati sul buffer
						key.interestOps(SelectionKey.OP_READ); // permette la ricezione al client
					}
				} catch (IOException e) {
					System.err.println(e.getMessage());
					key.cancel();	//elimina la chiave
					try {
						key.channel().close();	//chiude la connessione
					} catch (IOException ex) {
					}
				}
			}
		}
	}
}
