/*
 * Leonardo Vona
 * 545042
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MainClass {

	private static final int PORT = 6789; // porta per l'accesso al servizio dall'esterno

	public static void main(String[] args) {
		ServerSocket server = null;
		try {
			server = new ServerSocket(PORT); // crea un nuovo servizio in ascolto sulla porta definita
		} catch (IOException e) { // la porta è già occupata
			System.err.println("Porta " + PORT + " occupata");
			System.exit(1);
		}

		boolean done = false;
		while (!done) { // ciclo infinito
			try {
				Socket socket = server.accept(); // accetta una request da un client
				// passa la request ad un task gestito da un thread
				Thread service = new Thread(new ClientService(socket));
				service.start(); // avvia il thread
			} catch (IOException e) { // eccezioni non gestite
				System.err.println(e.getMessage());
			}
		}

		try { // chiude il server
			server.close();
		} catch (IOException e) { // errore durante la chiusura del servizio
			e.printStackTrace();
		}
	}
}
