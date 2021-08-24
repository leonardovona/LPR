/*
 * Leonardo Vona
 * 545042
 */

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Random;

public class PingServer {
	/*
	 * PingServer port [seed] 
	 * port: numero che indica la porta di ascolto del server
	 * seed: numero che indica il seme per la generazione casuale di latenze e perdite di pacchetti
	 */
	private static final int BUFSIZE = 100; // dimensione del buffer di input / output
	private static final int DELAY = 1000; // valore massimo per la simulazione di latenze

	public static void main(String[] args) {
		int port = 0; // porta scelta dall'utente
		long seed; // seme scelto dall'utente
		Random generator = null; // generatore di numeri casuali

		if (args.length != 2 && args.length != 1) { // non sono stati passati 1 o 2 argomenti al programma
			System.out.println("Usage: java PingServer port [seed]");
			System.exit(1);
		}

		try {
			port = Integer.parseInt(args[0]);
			if (port < 1024 || port > 65535) { // la porta non è valida
				throw new NumberFormatException();
			}
		} catch (NumberFormatException e) { // l'argomento passato non è un numero
			System.err.println("ERR -arg 1");
			System.exit(1);
		}

		try {
			if (args.length == 2) { // se è stato passato il seme
				seed = Long.parseLong(args[1]);
				generator = new Random(seed); // instanzia un generatore con seme dato
			} else {
				generator = new Random(); // instanzia un generatore
			}
		} catch (NumberFormatException e) { // l'argomento passato non è un numero
			System.err.println("ERR -arg 2");
			System.exit(1);
		}

		DatagramSocket socket = null; // socket per l'invio / ricezione di messaggi
		try {
			socket = new DatagramSocket(port); // apre la socket sulla porta port
		} catch (SocketException e) {
			System.err.println("Errore durante l'apertura della connessione");
		}
		System.out.println("Server accepting connections on port " + port);
		boolean end = false;
		while (!end) { // ciclo infinito
			byte[] buffer = new byte[BUFSIZE]; // buffer per l'invio / ricezione di messaggi
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length); // associa il buffer ad un packet
			try {
				socket.receive(packet); // attende un messaggio
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
			String s = null; // messaggio ricevuto
			try {
				s = new String(packet.getData(), 0, packet.getLength(), "UTF-8"); // effettua il parse del messaggio
			} catch (UnsupportedEncodingException e) {
				System.err.println(e.getMessage());
			}
			String[] data = s.split(" "); // suddivide il messaggio
			double discard = generator.nextDouble(); // stabilisce se il pacchetto deve essere scartato
			if (discard < 0.25) { // il pacchetto deve essere scartato
				System.out.println(packet.getAddress().getHostAddress() + ":" + packet.getPort() + " PING " + data[1]
						+ " " + data[2] + " ACTION: not sent");
			} else { // il pacchetto deve essere reinviato
				long delay = generator.nextInt(DELAY); // genera ritardo casuale

				try {
					Thread.sleep(delay); // va in attesa per delay ms
				} catch (InterruptedException e) {
					System.err.println(e.getMessage());
				}
				System.out.println(packet.getAddress().getHostAddress() + ":" + packet.getPort() + " PING " + data[1]
						+ " " + data[2] + " ACTION: delayed " + delay + " ms");
				buffer = s.getBytes(); // prepara il buffer per il reinvio
				try {
					socket.send(packet); // reinvia il messaggio al client
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}
			}
		}
		socket.close();
	}
}
