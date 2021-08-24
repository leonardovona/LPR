/*
 * Leonardo Vona
 * 545042
 */
package wordquizzle.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import wordquizzle.common.Configuration;

/*
 * Gestore della comunicazione tramite TCP
 */
public class ClientTCP {

	private SocketChannel channel; // canale di comuncazione
	private ByteBuffer buffer; // buffer per ricezione / invio messaggi

	public ClientTCP() throws IOException {
		// crea una connessione con il server sulla porta definita
		SocketAddress address = new InetSocketAddress(Configuration.SERVER_ADDRESS, Configuration.TCP_SERVER_PORT);
		channel = SocketChannel.open(address); // apre la connessione
		try {
			channel.configureBlocking(true); // si blocca sulla read
		} catch (IOException e) {
			e.printStackTrace();
		}
		buffer = ByteBuffer.allocate(1024); // crea buffer
	}

	// recupera la porta su cui è in ascolto per le connessioni TCP
	public int getLocalPort() {
		try {
			return Integer.parseInt(channel.getLocalAddress().toString().split(":")[1]);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

	// Legge dal canale
	public String read() {
		buffer.clear();
		StringBuilder sb = new StringBuilder();
		int bytesRead = 0;
		try {
			bytesRead = channel.read(buffer); // legge dal canale e memorizza sul buffer
		} catch (IOException e) {
			System.err.println("Connection interrupted from server");
			return "";
		}
		if (bytesRead == -1) { // connessione chiusa
			System.err.println("Connection interrupted from server");
			return "";
		}
		buffer.flip();
		while (buffer.hasRemaining()) {
			sb.append((char) buffer.get()); // recupera il messaggio un byte alla volta
		}
		buffer.clear();

		return sb.toString(); // ritorna il messaggio
	}

	// Invia sul canale
	public void send(String message) {
		byte[] bytes = message.getBytes();

		// invio della stringa al server
		buffer.put(bytes); // immette la stringa nel buffer
		buffer.flip(); // imposta il buffer per la lettura
		try {
			channel.write(buffer); // scrive sul canale con il server
		} catch (IOException e) {
			System.err.println("Connection interrupted from server");
			return;
		}
	}
}
