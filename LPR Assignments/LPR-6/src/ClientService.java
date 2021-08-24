/*
 * Leonardo Vona
 * 545042
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/*
 * Task che gestisce una richiesta dal client e invia la risposta
 */
public class ClientService implements Runnable {
	protected Socket socket; // socket relativa alla request
	private String filename; // nome del file da trasferire
	private String contentType; // tipo del file da trasferire

	public ClientService(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			// recupera il reader dalla socket
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String line = reader.readLine(); // legge la prima riga della request
			byte[] response; // response da inviare al client
			try {
				response = parseRequest(line); // recupera il file
			} catch (IOException e) { // errore durante la lettura del file
				contentType = "plain\text";
				response = "Errore durante la lettura del file".getBytes();
			}
			OutputStream os = socket.getOutputStream(); // recupera lo stream in output dalla socket
			// crea l'header di risposta
			String header = "HTTP/1.1 200 OK\r\n" + "Content-Type: " + contentType + "\r\n" + "Content-Length: "
					+ response.length + "\r\n" + "Connection: close\r\n" + "\r\n";

			os.write(header.getBytes()); // invia l'header
			os.write(response); // invia la response
			os.flush();
			os.close(); // chiude lo stream in uscita
			socket.close(); // chiude la socket

		} catch (Exception e) { // eccezioni non gestite
			System.err.println(e.getMessage());
		}
	}

	/*
	 * Recupera il file e lo prepara per l'invio
	 */
	private byte[] parseRequest(String line) throws Exception {
		String[] request = line.split(" "); // split della prima riga della request

		if (!request[0].equals("GET")) { // la request non è di tipo GET
			throw new Exception("Richiesta non supportata");
		}

		this.filename = request[1].substring(1); // recupera il nome del file dalla request
		Path filePath = Paths.get("./" + filename); // path relativo al file
		this.contentType = Files.probeContentType(filePath); // ricava il tipo del file

		// apre il canale per la lettura del file
		FileChannel inChannel = FileChannel.open(Paths.get(filename), StandardOpenOption.READ);

		// alloca un buffer di dimensione pari alla lunghezza del file
		ByteBuffer buffer = ByteBuffer.allocate((int) inChannel.size());
		inChannel.read(buffer); // legge dal canale e memorizza sul buffer

		inChannel.close(); // chiude il canale

		return buffer.array(); // restituisce il file sottoforma di array di byte
	}

}
