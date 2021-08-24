/*
 * Leonardo Vona
 * 545042
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/*
 * Client del multiplexed server
 */
public class EchoClient {
	//EchoClient HOST [PORT]
	//HOST: indirizzo ip della macchina dove risiede il processo server
	//PORT (opzionale): intero che indica la porta del processo server
	public static final int DEFAULT_PORT = 1919; // porta di default

	public static void main(String[] args) {
		if (args.length == 0) { // deve essere specificato l'host dove risiede il processo server
			System.out.println("Usage: java EchoClient host [port]");
			return;
		}

		int port; // porta scelta dall'utente
		try {
			port = Integer.parseInt(args[1]); // se passata come argomento del programma, imposta la porta definita dall'utente
		} catch (RuntimeException ex) {
			port = DEFAULT_PORT; // porta non definita, viene impostata quella di default
		}

		try {
			SocketAddress address = new InetSocketAddress(args[0], port); // crea una connessione con il server sulla porta definita
			SocketChannel client = SocketChannel.open(address); // apre la connessione
			client.configureBlocking(true);

			boolean end = false;
			while (!end) { // ciclo infinito

				// legge una stringa da tastiera, se la stringa è vuota il programma termina
				BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
				String msg = reader.readLine();
				byte[] bytes = msg.getBytes();
				if (msg.equals("")) { // chiusura connessione
					break;
				}

				// invio della stringa al server
				ByteBuffer buffer = ByteBuffer.allocate(msg.length());
				buffer.put(bytes); // immette la stringa nel buffer
				buffer.flip(); // imposta il buffer per la lettura
				try {
					client.write(buffer); // scrive sul canale con il server
				} catch (IOException e) {
					System.err.println("Connection interrupted from server");
					break;
				}
				// leggo da server
				buffer.clear(); // resetta il buffer
				buffer.flip(); // imposta il buffer per la scrittura
				/*
				 * int b;
				 * 
				 * 
				 * while ((b = client.read(buffer)) != 0) { //legge if (b == -1) {
				 * System.out.println("Server terminato, chiusura del client...\r\n");
				 * client.close(); return; } }
				 */

				client.read(buffer); // riceve una risposta dal server
				buffer.flip(); // imposta il buffer per la lettura
				String s = new String(buffer.array()); // recupera la risposta dal server
				buffer.clear(); // resetta il buffer
				System.out.println("Message from server: " + s);

			}

			client.close(); // chiude la connessione
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}