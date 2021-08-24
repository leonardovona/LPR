/*
 * Leonardo Vona
 * 545042
 */
package wordquizzle.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

//gestore della connessione UDP
public class ClientUDP implements Runnable {

	private int port; // porta sulla quale è in ascolto
	private ClientHandler handler; // gestore delle richieste

	public ClientUDP(int port, ClientHandler handler) {
		this.handler = handler;
		this.port = port;
	}

	@Override
	public void run() {
		byte[] buffer = new byte[512];
		try {
			DatagramSocket socket = new DatagramSocket(port); // crea la socket UDP
			DatagramPacket packet;
			String request, response;
			while (true) {
				packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet); // attende la ricezione di un pacchetto
				request = new String(packet.getData(), 0, packet.getLength(), "UTF-8"); // recupera la richiesta
				response = handler.challengeRequest(request); // mostra la richiesta di sfida all'utente
				packet = new DatagramPacket(response.getBytes(StandardCharsets.UTF_8), response.length(),
						packet.getAddress(), packet.getPort()); // pacchetto per la risposta
				socket.send(packet); // invia la risposta al server
				if (response.equals("OK")) { // se la sfida è stata accettata passa alla sfida
					handler.acceptedChallenge();
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
