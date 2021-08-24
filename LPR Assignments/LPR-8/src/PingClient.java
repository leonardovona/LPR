/*
 * Leonardo Vona
 * 545042
 */

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;

public class PingClient {
	/*
	 * PingClient hostname port 
	 * hostname: indirizzo ip del server 
	 * port: porta su cui è in ascolto il server
	 */
	private static final int TIMEOUT = 2000; // tempo massimo di attesa per la ricezione del messaggio di risposta
	private static final int BUFSIZE = 100; // dimensione del buffer di input
	private static final int NUMPKS = 10; // numero di pacchetti da inviare

	public static void main(String[] args) {
		InetAddress address = null; // indirizzo del server
		int port = 0; // porta del server
		int receivedPackets = 0; // numero di pacchetti ricevuti
		double avgRTT = 0; // RTT medio
		long minRTT = 0; // RTT minimo
		long maxRTT = 0; // RTT massimo

		if (args.length != 2) { // non sono stati passati 2 argomenti al programma
			System.out.println("Usage: java PingClient hostname port");
			System.exit(1);
		}
		try {
			address = InetAddress.getByName(args[0]);
		} catch (UnknownHostException e) { // non è stato possibile risolvere hostname
			System.err.println("ERR -arg 1");
			System.exit(1);
		}

		try {
			port = Integer.parseInt(args[1]);
			if (port < 1024 || port > 65535) { // la porta indicata non è valida
				throw new NumberFormatException();
			}
		} catch (NumberFormatException e) { // il parametro passato non è un numero
			System.err.println("ERR -arg 2");
			System.exit(1);
		}

		DatagramSocket socket = null; // socket per l'invio / ricezione di pacchetti al server
		try {
			socket = new DatagramSocket(); // inizializza socket
			socket.setSoTimeout(TIMEOUT); // imposta il timeout di attesa per la ricezione di pacchetti
		} catch (SocketException e) {
			System.err.println(e.getMessage());
		}

		byte[] buffer = null; // buffer per l'invio di messaggi
		for (int i = 0; i < NUMPKS; i++) { // invia i pacchetti
			long sendTime = System.currentTimeMillis(); // tempo di invio in ms
			String msg = "PING " + i + " " + sendTime; // prepara il messaggio
			try {
				buffer = msg.getBytes("UTF-8"); // prepara il buffer
			} catch (UnsupportedEncodingException e) {
				System.err.println(e.getMessage());
			}
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port); // crea un pacchetto per l'invio
			try {
				socket.send(packet); // invio il pacchetto
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}

			byte[] inputBuffer = new byte[BUFSIZE]; // buffer per la ricezione di pacchetti
			packet = new DatagramPacket(inputBuffer, inputBuffer.length); // crea un pacchetto per la ricezione
			try {
				socket.receive(packet); // attende la ricezione di un pacchetto per massimo TIMEOUT ms

				String s = new String(packet.getData(), 0, packet.getLength(), "UTF-8"); // converte i dati ricevuti in stringa

				String[] data = s.split(" "); // suddivide in messaggio
				if (Integer.parseInt(data[1]) == i) { // il pacchetto ricevuto è quello atteso
					long RTT = System.currentTimeMillis() - sendTime; // calcola RTT
					System.out.println(s + " RTT: " + RTT + " ms");
					receivedPackets++; // incrementa il n. di pacchetti ricevuti
					avgRTT += RTT; // aggiunge RTT alla media
					if (minRTT == 0 || minRTT > RTT) { // controlla RTT minimo
						minRTT = RTT;
					}

					if (maxRTT < RTT) { // controlla RTT massimo
						maxRTT = RTT;
					}
				}
			} catch (SocketTimeoutException e) { // non è stata ricevuta risposta entro TIMEOUT ms
				System.out.println("PING " + i + " " + sendTime + " RTT: *");
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
		socket.close(); // chiude la socket
		avgRTT = avgRTT / receivedPackets; // calcola la media

		int lostPackets = NUMPKS - receivedPackets; // calcola i pacchetti persi
		float lossPercentage = lostPackets * 100 / NUMPKS; // calcola la percentuale di perdita

		System.out.println("\t\t---- PING Statistics ----");
		System.out.println(NUMPKS + " packets transmitted, " + receivedPackets + " packets received, " + lossPercentage
				+ "% packet loss");

		DecimalFormat df = new DecimalFormat(); // utilizzato per stampare double con massimo 2 decimali
		df.setMaximumFractionDigits(2); // imposta numero massimo di decimali

		System.out.println("round-trip (ms) min/avg/max = " + minRTT + "/" + df.format(avgRTT) + "/" + maxRTT);
	}
}