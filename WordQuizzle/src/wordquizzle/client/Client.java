/*
 * Leonardo Vona
 * 545042
 */
package wordquizzle.client;

import java.io.IOException;

/*
 * Classe principale del client
 */
public class Client {

	public static void main(String[] args) {
		ClientHandler clientHandler = new ClientHandler(); // gestore delle richieste

		ClientTCP clientTCP; // gestore della connessione TCP
		try {
			clientTCP = new ClientTCP();
		} catch (IOException e) { // connessione al server fallita
			System.err.println("The server is currently not responding");
			return;
		}
		ClientGUI clientGUI = new ClientGUI(clientHandler); // gestore della GUI
		clientHandler.setGUI(clientGUI);
		clientHandler.setTCP(clientTCP);

		ClientUDP clientUDP = new ClientUDP(clientTCP.getLocalPort(), clientHandler); // gestore della connessione UDP
		(new Thread(clientUDP)).start();
	}
}