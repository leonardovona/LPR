/*
 * Leonardo Vona
 * 545042
 */
package wordquizzle.server;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Iterator;
import java.util.Random;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import wordquizzle.common.Configuration;
import wordquizzle.common.User;

//gestore di una sfida
public class ChallengeManager implements Runnable {

	// classe che rappresenta i dati della sfida di un utente
	private class ChallengeData {
		public User user; // utente che partecipa alla sfida
		public int currWord; // parola attuale che deve tradurre
		public int correctAnswers; // risposte corrette
		public boolean finished; // true se l'utente ha terminato le traduzioni
		public long lastAnswer; // timestamp dell'ultima risposta

		public ChallengeData(User user) {
			this.user = user;
			this.finished = false;
		}
	}

	// classe che rappresenta una coppia <parola, traduzione>
	private class Translation {
		public String word;
		public String translation;

		public Translation(String word) throws IOException {
			this.word = word;
			translate();
//			Decommentare la seguente riga per vedere le traduzioni
//			System.out.println(word + " | " + translation);
		}

		// traduce la parola
		private void translate() throws IOException {
			// forma la richiesta
			URL url = new URL(
					"https://api.mymemory.translated.net/get?q=" + URLEncoder.encode(word, "UTF8") + "&langpair=it|en");
			// riceve la risposta e la interpreta come JSON
			JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(url.openStream())));
			// parse della risposta ricevuta
			JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
			// recupera la traduzione dalla risposta ricevuta
			translation = jsonObject.get("responseData").getAsJsonObject().get("translatedText").getAsString().toLowerCase();
		}

	}

	private ChallengeData[] users = new ChallengeData[2]; // dati degli utenti in sfida
	private List<Translation> translations; // coppie <parola, traduzione>
	private boolean finished = false; // la sfida è terminata

	public ChallengeManager(User user1, User user2) throws IOException {
		this.translations = new ArrayList<>(Configuration.K);
		this.users[0] = new ChallengeData(user1);
		this.users[1] = new ChallengeData(user2);
		try {
			for (String word : getWords()) { // seleziona le parole da tradurre e recupera la traduzione
				translations.add(new Translation(word));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		Selector selector = null; // selettore interno alla sfida
		try {
			selector = Selector.open();
			setup(0, selector); // registra i client sul selettore
			setup(1, selector);
			long startTime = System.currentTimeMillis(); // registra il tempo di avvio della partita
			while (System.currentTimeMillis() - startTime <= Configuration.T2 && !finished) { // finchè il tempo non è
																								// scaduto o la sfida
																								// non è finita
				selector.select(10);
				Set<SelectionKey> readyKeys = selector.selectedKeys(); // recupera le chiavi da gestire
				Iterator<SelectionKey> iterator = readyKeys.iterator();
				while (iterator.hasNext()) {
					SelectionKey key = iterator.next(); // recupera la chiave
					iterator.remove(); // rimuove la chiave dall'iteratore
					if (key.isReadable()) // riceve qualcosa da un client
						if (!read(key)) {
							break;
						}
					if (key.isWritable()) { // deve inviare qualcosa ad un client
						if (!write(key))
							break;
					}
				}
			}

			String response = endChallenge(); // calcola i punteggi finali
			for (SelectionKey key : selector.keys()) { // invia i risultati agli utenti

				((ByteBuffer) key.attachment()).clear();
				((ByteBuffer) key.attachment()).put(response.getBytes());
				((ByteBuffer) key.attachment()).flip();
				write(key);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CancelledKeyException e) { // Uno degli utenti ha abbandonato la sfida
			String message = "1\nGame over";
			for (SelectionKey key : selector.keys()) { // invia messaggio di termine partita

				((ByteBuffer) key.attachment()).clear();
				((ByteBuffer) key.attachment()).put(message.getBytes());
				((ByteBuffer) key.attachment()).flip();
				try {
					write(key);
				} catch (IOException e1) {
				}
			}
		}
	}

	// legge una richiesta da un client
	private boolean read(SelectionKey key) throws IOException, CancelledKeyException {
		SocketChannel client = (SocketChannel) key.channel();
		ByteBuffer buffer = (ByteBuffer) key.attachment();
		StringBuilder sb = new StringBuilder();
		int bytesRead = client.read(buffer); // legge la richiesta e la memorizza nel buffer
		if (bytesRead == -1) { // la connessione è stata interrotta
			finished = true;
			key.cancel();
			return false;
		}
		while (bytesRead > 0) { // legge il messaggio
			buffer.flip();
			while (buffer.hasRemaining()) {
				sb.append((char) buffer.get()); // trasferisce il messaggio dal buffer un byte alla volta
			}
			buffer.clear();
			bytesRead = client.read(buffer);
		}
		handle(new StringTokenizer(sb.toString(), "\n"), key); // gestisce la richiesta
		return true;
	}

	// invia una risposta a un client
	private boolean write(SelectionKey key) throws IOException, CancelledKeyException {
		SocketChannel client = (SocketChannel) key.channel();
		ByteBuffer buffer = (ByteBuffer) key.attachment();

		if (!buffer.hasRemaining()) {
			return false;
		}
		while (buffer.hasRemaining()) {
			client.write(buffer); // ivnia la risposta memorizzata sul buffer
		}
		buffer.clear();
		key.interestOps(SelectionKey.OP_READ);
		return true;
	}

	// gestisce una richiesta
	private void handle(StringTokenizer request, SelectionKey key) throws CancelledKeyException {
		ByteBuffer buffer = (ByteBuffer) key.attachment();
		String response;
		// Comando inviato dal client
		String command = request.nextToken(); // recupera il comando richiesto dal client
		switch (command) {
		case "WORD": // il client ha inviato una parola
			String username = request.nextToken(); // recupera l'username
			String word = "";
			try {
				word = request.nextToken(); // recupera la parola
			} catch (NoSuchElementException e) { // la parola è vuota
				word = "";
			}
			response = word(username, word); // controlla la traduzione e invia una nuova parola
			break;
		case "QUIT": // il client vuole uscire dalla partita
			throw new CancelledKeyException();
		default: // Operazione richiesta non supportata
			response = "1\nUnsupported Operation";
		}
		if (!finished) { // se la partita non è finita
			buffer.put(response.getBytes()); // invia una nuova parola al client
			key.interestOps(SelectionKey.OP_WRITE); // imposta una richiesta di scrittura verso il client
		}
		buffer.flip();

	}

	// verifica la traduzione ricevuta e prepara una nuova parola da inviare
	private String word(String username, String translation) {
		long answerTime = System.currentTimeMillis(); // timestamp associato alla ricezione della parola
		ChallengeData user;
		if (users[0].user.getUsername().equals(username)) // recupera l'utente che ha inviato la sfida
			user = users[0];
		else if (users[1].user.getUsername().equals(username))
			user = users[1];
		else // errore nel messaggio inviato
			return "1\nError parsing request";
		// verifica se la traduzione inviata è corretta ed è stata inviata in tempo
		if (translations.get(user.currWord).translation.equals(translation.toLowerCase())
				&& System.currentTimeMillis() - user.lastAnswer < Configuration.T1)
			user.correctAnswers++;

		if (user.currWord == Configuration.K - 1) { // le parole sono terminate
			user.finished = true; // l'utente ha terminato
			if (users[0].finished && users[1].finished) { // la sfida è finita
				finished = true;
				return null;
			} else // informa l'utente che ha terminato
				return "2\nIn attesa che il tuo avversario termini la partita.";
		} else { // ci sono altre parole
			user.lastAnswer = answerTime; // aggiorna il timestamp dell'utlima risposta
			String nextWord = translations.get(++user.currWord).word; // recupera una nuova parola
			return "0\n" + nextWord;
		}
	}

	// recupera da file un elenco di parole da tradurre
	private List<String> getWords() throws FileNotFoundException {
		int listSize = Configuration.K; // numero di parole da recuperare
		String currentLine = null;
		List<String> wordsList = new ArrayList<>(listSize);
		int count = 0;
		Random ra = new Random();
		int randomNumber = 0;
		Scanner sc = new Scanner(new File(Configuration.WORDS_FILE)).useDelimiter("\n");
		while (sc.hasNext()) { // legge il file una riga alla volta
			currentLine = sc.next();
			count++;
			if (count <= listSize) // la parola deve essere aggiunta
				wordsList.add(currentLine);
			else if ((randomNumber = (int) ra.nextInt(count)) < listSize) // fattore randomico
				wordsList.set(randomNumber, currentLine); // aggiorna l'elemento
		}
		sc.close();
		return wordsList;
	}

	// calcola i punteggi finali
	private String endChallenge() {
		int points0 = Configuration.X * users[0].correctAnswers
				- Configuration.Y * (Configuration.K - users[0].correctAnswers); // calcolo punteggi
		int points1 = Configuration.X * users[1].correctAnswers
				- Configuration.Y * (Configuration.K - users[1].correctAnswers);
		if (points0 < 0) // se i punti sono negativi vengono azzerati
			points0 = 0;
		if (points1 < 0)
			points1 = 0;
		if (points0 > points1) // determina il vincitore
			points0 += Configuration.Z;
		else if (points1 > points0)
			points1 += Configuration.Z;
		else { // pareggio, i punti bonus vengono assegnati a metà
			points0 += Configuration.Z / 2;
			points1 += Configuration.Z / 2;
		}
		users[0].user.addPoints(points0); // aggiunge i punti agli utenti
		users[1].user.addPoints(points1);
		
		int code;
		if(users[0].finished && users[1].finished)
			code = 3;
		else
			code = 4;
		
		return code + "\n" + users[0].user.getUsername() + ":" + users[0].correctAnswers + ":" + users[1].user.getUsername()
				+ ":" + users[1].correctAnswers;
	}

	// registra il client sul selettore ed invia la prima parola
	private void setup(int index, Selector selector) {
		SocketChannel channel = (SocketChannel) users[index].user.getKey().channel();
		SelectionKey key = null;
		try {
			key = channel.register(selector, SelectionKey.OP_WRITE); // registra il client sul selettore
		} catch (ClosedChannelException e) {
			e.printStackTrace();
			return;
		}
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		buffer.put(("0\n" + translations.get(users[index].currWord).word).getBytes()); // recupera la prima parola e la
																						// invia
		buffer.flip();
		key.attach(buffer);
		users[index].lastAnswer = System.currentTimeMillis(); // imposta il timer per il timeout di risposta
	}
}
