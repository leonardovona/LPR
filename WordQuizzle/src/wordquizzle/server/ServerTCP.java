/*
 * Leonardo Vona 
 * 545042
 */

package wordquizzle.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import wordquizzle.common.Configuration;
import wordquizzle.common.User;

/*
 * Classe per la gestione della comunicazione tramite TCP del server
 */
public class ServerTCP {
	private Map<String, User> users; // utenti dell'applicazione
	private Selector selector; // selettore per la gestione delle connessioni
	private ThreadPoolExecutor executor; // thread pool per la gestione delle richieste

	public ServerTCP(Map<String, User> users) {
		this.users = users;
		// crea un fixed thread pool
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Configuration.THREAD_POOL_SIZE);
		ServerSocketChannel serverSocketChannel; // canale di comunicazione
		try {
			serverSocketChannel = ServerSocketChannel.open();
			// bind del processo server alla porta definita
			serverSocketChannel.socket().bind(new InetSocketAddress(Configuration.TCP_SERVER_PORT));
			serverSocketChannel.configureBlocking(false); // imposta che non sia bloccante
			selector = Selector.open();
			// registra l'evento di accettazione di una connessione
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		System.out.println("Listening for connections on port " + Configuration.TCP_SERVER_PORT);
		while (true) {
			try {
				selector.select(10); // seleziona le chiavi da gestire, se ci sono
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
			Set<SelectionKey> readyKeys = selector.selectedKeys(); // recupera le chiavi da gestire
			Iterator<SelectionKey> iterator = readyKeys.iterator();
			while (iterator.hasNext()) {
				SelectionKey key = iterator.next(); // recupera la chiave
				iterator.remove(); // rimuove la chiave dall'iteratore
				try {
					if (key.isAcceptable())// accettazione della connessione
						accept(key);
					if (key.isReadable()) // riceve qualcosa da un client
						if (!read(key))
							break;
					if (key.isWritable()) { // deve inviare qualcosa ad un client
						if (!write(key))
							break;
					}
				} catch (IOException e) {
					e.printStackTrace();
					key.cancel(); // elimina la chiave
					try {
						key.channel().close(); // chiude la connessione
					} catch (IOException ex) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void accept(SelectionKey key) throws IOException {
		ServerSocketChannel server = (ServerSocketChannel) key.channel();
		SocketChannel client = server.accept(); // accetta la connessione
		System.out.println("Accepted connection from " + client);
		client.configureBlocking(false);
		SelectionKey key2 = client.register(selector, SelectionKey.OP_READ); // registra la lettura da parte del client
		ByteBuffer buffer = ByteBuffer.allocate(1024); // buffer relativo al client
		key2.attach(buffer);
	}

	private boolean read(SelectionKey key) throws IOException {
		SocketChannel client = (SocketChannel) key.channel();
		ByteBuffer buffer = (ByteBuffer) key.attachment();
		StringBuilder sb = new StringBuilder(); // String builder per recuperare il messaggio dal buffer
		int bytesRead;
		try {
			bytesRead = client.read(buffer); // legge il messaggio dal canale e lo memorizza nel buffer
		} catch (IOException e) {
			e.getMessage();
			closeConnection(key);
			return false;
		}
		if (bytesRead == -1) { // la connessione è stata interrotta
			closeConnection(key);
			return false;
		}
		while (bytesRead > 0) { // legge fino a quando c'è qualcosa da leggere
			buffer.flip();
			while (buffer.hasRemaining()) {
				sb.append((char) buffer.get()); // copia sullo string builder il messaggio un byte alla volta
			}
			buffer.clear();
			bytesRead = client.read(buffer);
		}
		executor.execute(new RequestHandler(sb.toString(), key, users)); // gestisce la richiesta
		return true;
	}

	private boolean write(SelectionKey key) throws IOException {
		SocketChannel client = (SocketChannel) key.channel();
		ByteBuffer buffer = (ByteBuffer) key.attachment();
		buffer.flip(); // imposta il buffer per la lettura

		if (!buffer.hasRemaining()) {
			return false;
		}
		while (buffer.hasRemaining()) {
			client.write(buffer); // scrive sul canale il messaggio sul buffer
		}
		buffer.clear();
		key.interestOps(SelectionKey.OP_READ);
		return true;
	}

	private void closeConnection(SelectionKey key) {
		key.cancel();
		executor.execute(new RequestHandler("CLOSE", key, users)); // fa il logout dell'utente associato al canale
																	// dall'applicazione
	}
}
