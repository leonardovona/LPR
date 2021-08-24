/*
 * Leonardo Vona
 * 545042
 */

package wordquizzle.server;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import wordquizzle.common.FriendException;
import wordquizzle.common.RankEntry;
import wordquizzle.common.User;

/*
 * Classe che gestisce una richiesta e crea una risposta per il client
 */
public class RequestHandler implements Runnable {
	private ByteBuffer buffer; // buffer associato al canale con il client
	private SelectionKey key; // chiave associata al canale
	private StringTokenizer request; // richiesta pervenuta dal client
	private static Map<String, User> users; // utenti nell'applicazione
	private static Gson gson; // oggetto per la serializzazione tramite JSON

	public RequestHandler(String request, SelectionKey key, Map<String, User> users) {
		this.request = new StringTokenizer(request);
		this.key = key;
		this.buffer = (ByteBuffer) key.attachment();
		RequestHandler.users = users;
		RequestHandler.gson = new GsonBuilder().setPrettyPrinting().create();
	}

	@Override
	public void run() {
		String response;
		String command = request.nextToken(); // Comando inviato dal client
		switch (command) {
		case "LOGIN":
			response = login(); // effettua il login sulla piattaforma
			break;
		case "FRIENDS":
			response = friendsList(); // restituisce la lista degli amici in formato JSON
			break;
		case "LOGOUT":
			response = logout(); // effettua il logout dalla piattaforma
			break;
		case "ADDFRIEND":
			response = addFriend(); // aggiunge un amico
			break;
		case "CHALLENGE":
			response = challenge(); // avvia una richiesta di sfida
			break;
		case "POINTS":
			response = points(); // restituisce il numero di punti
			break;
		case "RANK":
			response = rank(); // restituisce la classifica
			break;
		case "CLOSE": // comando inviato dal server TCP in caso la connessione sia stata chiusa
			logout();
			return;
		case "WORD": // comando interno della sfida, ignorato a questo livello
			response = "1\nYour friend left the game";
			break;
		case "QUIT": // comando interno della sfida, ignorato a questo livello
			return;
		default: // comando non riconosciuto
			response = "2\nUnsupported Operation";
		}
		if (response.startsWith("9"))
			return; // messaggio da ignorare, da non inviare al client
		buffer.put(response.getBytes()); // immette la risposta nel buffer associato al canale
		key.interestOps(SelectionKey.OP_WRITE); // imposta una richiesta di scrittura verso il client
	}

	// login di un utente all'interno della piattaforma
	private String login() {
		User user;
		try {
			user = users.get(request.nextToken()); // recupera l'utente tramite l'username passato con la richiesta
		} catch (NoSuchElementException e) { // la richiesta non è stata formata correttamente
			return "1\nUnvalid username";
		}
		String password;
		try {
			password = request.nextToken(); // recupera la password dalla richiesta
		} catch (NoSuchElementException e) { // la richiesta non è stata formata correttamente
			return "1\nLogin failed";
		}

		if (user == null) // l'utente non è stato trovato
			return "1\nUnvalid username";
		else if (user.isLogged()) // l'utente è già loggato nella piattaforma
			return "1\nUser is already logged";
		else if (!user.passwordMatch(password)) // la password non è corretta
			return "1\nLogin failed";
		user.login(key); // effettua il login
		return "0\nLogin successful"; // messaggio di conferma
	}

	// effettua il logout dalla piattaforma
	private String logout() {
		String username;
		try {
			username = request.nextToken(); // recupera l'username passato con la richiesta
		} catch (NoSuchElementException e) {// la richiesta non è stata formata correttamente
			return "1\nUnvalid username";
		}
		User user = users.get(username); // recupera l'utente associato all'username
		if (user == null) // l'utente non è stato trovato
			return "1\nUnvalid username";
		else if (!user.isLogged()) // l'utente non è loggato
			return "1\nUser is already logged out";
		user.logout(); // effettua il logout
		return "0\nLogout successful"; // messaggio di conferma
	}

	// aggiunge un amico
	private String addFriend() {
		User user = users.get(request.nextToken()); // recupera l'username dell'utente che richiede l'operazione
		User friend;
		try {
			friend = users.get(request.nextToken()); // recupera l'username dell'amico da aggiungere
		} catch (NoSuchElementException e) { // richiesta mal formata
			return "1\nUnvalid friend username";
		}

		if (user == null) // utente richiedente non trovato
			return "1\nUnvalid username";
		else if (!user.isLogged()) // utente richiedente non è loggato
			return "1\nYou are not logged";
		else if (friend == null) // amico non trovato
			return "1\nUnvalid friend username";
		try {
			// crea il legame di amicizia
			user.addFriend(friend);
			friend.addFriend(user);
		} catch (FriendException e) { // errore durante l'aggiunta dell'amico
			return "1\n" + e.getMessage();
		}
		save(); // memorizza i cambiamenti su file
		return "0\nFriendship successful"; // messaggio di conferma
	}

	// restituisce la lista degli amici in formato JSON
	private String friendsList() {
		User user = users.get(request.nextToken()); // recupera l'utente richiedente
		if (user == null) // utente non trovato
			return "1\nUnvalid username";
		else if (!user.isLogged()) // l'utente non è loggato
			return "1\nYou are not logged";
		return "0\n" + gson.toJson(user.getFriends()); // restituisce in formato JSON la lista degli amici dell'utente
	}

	// restituisce il punteggio dell'utente
	private String points() {
		User user = users.get(request.nextToken()); // recupera l'utente richiedente
		if (user == null) // utente non trovato
			return "1\nUnvalid username";
		else if (!user.isLogged()) // utente non loggato
			return "1\nYou are not logged";
		return "0\n" + user.getPoints(); // restituisce il punteggio dell'utente richiedente
	}

	// restituisce una classifica in base al punteggio dell'utente richiedente e i
	// suoi amici
	private String rank() {
		User user = users.get(request.nextToken()); // recupera utente richiedente
		List<RankEntry> rank = new ArrayList<>(); // lista di elementi della classifica
		if (user == null) // utente non trovato
			return "1\nUnvalid username";
		else if (!user.isLogged()) // utente non loggato
			return "1\nYou are not logged";
		rank.add(new RankEntry(user.getUsername(), user.getPoints())); // aggiunge alla classifica l'utente richiedente
		for (String friend : user.getFriends()) // aggiunge alla classifica gli amici
			rank.add(new RankEntry(friend, users.get(friend).getPoints()));
		Collections.sort(rank, Collections.reverseOrder()); // ordina in modo decrescente la lista
		return "0\n" + gson.toJson(rank); // restituisce in formato JSON la classifica
	}

	// gestisce una richiesta per avviare una sfida
	private String challenge() {
		User user = users.get(request.nextToken()); // recupera l'utente sfidante
		User friend = users.get(request.nextToken()); // recupera l'utente sfidato
		if (user == null) // utente sfidante non trovato
			return "1\nUnvalid username";
		else if (!user.isLogged()) // utente sfidante non loggato
			return "1\nYou are not logged";
		else if (friend == null) // utente sfidato non trovato
			return "1\nUnvalid friend username";
		else if (!user.isFriend(friend.getUsername())) // utente sfidato non è amico di utente sfidante
			return "1\nYou are not friend with " + friend.getUsername();
		else if (!friend.isLogged()) // utente sfidato non loggato
			return "1\nYour friend is currently offline";
		if (!requestChallenge(user.getUsername(), friend.getKey())) // richiede all'utente sfidato se vuole accettare la
																	// sfida
			return "1\nRequest not accepted";

		user.getKey().interestOps(0); // disattiva temporaneamente le chiavi sel selettore principale degli utenti
										// sfidante e sfidato
		friend.getKey().interestOps(0);
		Thread t;
		try {
			t = new Thread(new ChallengeManager(user, friend)); // passa al gestore della sfida
		} catch (IOException e1) { // errore durante la preparazione della sfida
			String errorMessage = "1\nTranslation service currently unavailable";
			System.err.println("Translation service currently unavailable");
			((ByteBuffer) user.getKey().attachment()).put(errorMessage.getBytes()); // invia messaggio ai client
			((ByteBuffer) friend.getKey().attachment()).put(errorMessage.getBytes());
//			buffer.flip();
			user.getKey().interestOps(SelectionKey.OP_WRITE);
			friend.getKey().interestOps(SelectionKey.OP_WRITE);
			return "9\n";
		}
		t.start(); // avvia il gestore della sfida
		try {
			t.join(); // attende la terminazione della sfida
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		save(); // memorizza i cambiamenti su file
		user.getKey().interestOps(SelectionKey.OP_READ); // riabilita le chiavi del selettore principale per gli utenti
															// sfidante e sfidato
		friend.getKey().interestOps(SelectionKey.OP_READ);
		return "9\n";
	}

	// invia una richiesta di sfida tramite UDP all'utente sfidato
	private boolean requestChallenge(String username, SelectionKey friendKey) {
		SocketAddress friendAddress = null;
		try {
			friendAddress = ((SocketChannel) friendKey.channel()).getRemoteAddress(); // recupera l'indirizzo
																						// dell'utente sfidato
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			DatagramSocket socket = new DatagramSocket();
			// Pacchetto che contiene lo username
			DatagramPacket packet = new DatagramPacket(username.getBytes(StandardCharsets.UTF_8), username.length(),
					friendAddress);
			// Invia lo username sul socket all'utente sfidato
			socket.send(packet);
			// Setta il timeout per la ricezione
			socket.setSoTimeout(120000);

			byte[] buffer = new byte[2];
			packet = new DatagramPacket(buffer, buffer.length);
			socket.receive(packet); // Riceve il pacchetto
			String response = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
			socket.close();
			if (response.equals("OK")) { // lo sfidato ha accettato la sfida
				return true;
			} else { // lo sfidato non ha accettato la sfida
				return false;
			}
		} catch (SocketTimeoutException e) {
			((ByteBuffer) friendKey.attachment()).put("5\nTimeout".getBytes()); //in caso di risposta positiva, informa lo sfidato del timeout
			friendKey.interestOps(SelectionKey.OP_WRITE); // imposta una richiesta di scrittura verso lo sfidato
			return false;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	// salva i dati degli utenti su file JSON
	private static synchronized void save() {
		String json = gson.toJson(users); // serializza in JSON gli utenti
		try (Writer writer = new FileWriter("users.json")) {
			writer.write(json); // scrive su file
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}