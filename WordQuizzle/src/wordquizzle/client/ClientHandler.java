/*
 * Leonardo Vona
 * 545042
 */
package wordquizzle.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Type;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import wordquizzle.common.Configuration;
import wordquizzle.common.RankEntry;
import wordquizzle.server.SignIn;

/*
 * Oggetto che gestisce le richieste da inviare al server e le risposte ricevute.
 * Gestisce anche gli eventi generati dagli elementi della GUI
 */
public class ClientHandler implements ActionListener {

	private ClientTCP clientTCP; // gestore della connessione TCP
	private ClientGUI gui; // gestore della GUI

	private String username; // username dell'utente loggato

	private boolean challenging; // true se l'utente sta effettuando una sfida

	private Gson gson; // oggetto per la gestione di JSON
	private Type rankEntryListType = new TypeToken<List<RankEntry>>() {
	}.getType(); // tipo di un elemento della classifica
	private Type friendsSetType = new TypeToken<Set<String>>() {
	}.getType(); // tipo di un elemento della lista degli amici

	public ClientHandler() {
		this.gson = new Gson();
		this.challenging = false;
	}

	public void setTCP(ClientTCP clientTCP) {
		this.clientTCP = clientTCP;
	}

	public void setGUI(ClientGUI clientGUI) {
		this.gui = clientGUI;
	}

	// gestisce gli eventi generati dalla GUI
	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
		case "Login":
			login(); // effettua il login
			break;
		case "Create Account": // l'utente si vuole registrare
			gui.changePane("Sign In");
			break;
		case "Sign In":
			signIn(); // effettua la registrazione
			break;
		case "Cancel":
			gui.changePane("Login"); // ritorna dalla pagina di registrazione a quella di login
			break;
		case "Challenge": // l'utente vuole fare una sfida
			listFriends(); // mostra la lista degli amici
			gui.changePane("Challenge Request"); // mostra la pagina per la creazione di una sfida
			break;
		case "Add Friend": // l'utente vuole aggiungere un amico
			gui.changePane("Add Friend"); // mostra la pagina per aggiungere un amico
			break;
		case "Add":
			addFriend(); // aggiunge un amico
			break;
		case "Request challenge":
			requestChallenge(); // richiede una sfida
			break;
		case "Send":
			sendTranslation(); // invia una traduzione
			break;
		case "Logout":
			logout(); // effettua il logout
			break;
		case "Go Back":
			gui.changePane("Main Page"); // ritorna alla pagina principale
			ranking(); // ricarica la classifica
			break;
		case "Exit":
			quitChallenge(); // esce dalla sfida
			break;
		default: // richiesta non riconosciuta
			System.err.println("Unsupported Operation");
		}
	}

	// recupera il punteggio dell'utente
	private void score() {
		clientTCP.send("POINTS\n" + username); // invia la richiesta al server

		String message = clientTCP.read(); // riceve la risposta

		StringTokenizer response = new StringTokenizer(message, "\n"); // parsing della risposta
		switch (response.nextToken()) {
		case "0": // richiesta soddisfatta
			gui.scoreLabel.setText("Your score is: " + response.nextToken()); // imposta il punteggio
			break;
		case "1": // errore durante la richiesta
			gui.showError(response.nextToken());
			break;
		default: // errore di comunicazione
			gui.showError("Error comunicating with sever");
			break;
		}
	}

	// effettua il logout
	private void logout() {
		clientTCP.send("LOGOUT\n" + username); // invio richiesta

		String message = clientTCP.read(); // ricevuta risposta

		StringTokenizer response = new StringTokenizer(message, "\n"); // parsing risposta
		switch (response.nextToken()) {
		case "0": // richiesta soddisfatta
			gui.showMessage(response.nextToken());
			gui.changePane("Login"); // torna alla pagina di login
			this.username = null;
			break;
		case "1": // errore durante la richiesta
			gui.showError(response.nextToken());
			break;
		default: // errore di comunicazione
			gui.showError("Error comunicating with sever");
			break;
		}
	}

	// recupera la classifica
	private void ranking() {
		clientTCP.send("RANK\n" + username); // invia richiesta

		String message = clientTCP.read(); // ricevuta risposta

		switch (message.substring(0, 1)) { // codice messaggio
		case "0": // richiesta soddisfatta
			List<RankEntry> ranking = gson.fromJson(message.substring(2), rankEntryListType); // deserializza da JSON la
																								// classifica
			gui.rankingTable.setModel(new RankingTableModel(ranking)); // aggiorna la tabella con la classifica
			break;
		case "1": // errore durante la richiesta
			gui.showError(message.substring(2)); // messaggio di errore
			break;
		default: // errore di comunicazione
			gui.showError("Error comunicating with sever");
			break;
		}

	}

	// aggiunge un amico
	private void addFriend() {
		String friend = gui.friendUsernameField.getText(); // username dell'amico da aggiungere
		clientTCP.send("ADDFRIEND\n" + username + "\n" + friend); // invia la richiesta

		String message = clientTCP.read(); // riceve la risposta

		StringTokenizer response = new StringTokenizer(message, "\n"); // parsing
		switch (response.nextToken()) {
		case "0": // richiesta soddisfatta
			gui.showMessage(response.nextToken());
			gui.friendUsernameField.setText(""); // resetta campo username amico
			break;
		case "1": // errore durante la richiesta
			gui.showError(response.nextToken());
			break;
		default: // errore di comunicazione
			gui.showError("Error comunicating with sever");
			break;
		}
	}

	// effettua il login
	private void login() {
		String user = gui.usernameField.getText(); // recupera l'username
		String password = new String(gui.passwordField.getPassword()); // recupera la password

		clientTCP.send("LOGIN\n" + user + "\n" + password); // invia la richiesta

		String message = clientTCP.read(); // riceve la risposta

		StringTokenizer response = new StringTokenizer(message, "\n"); // parsing
		switch (response.nextToken()) {
		case "0": // login effettuato
			gui.showMessage(response.nextToken());
			gui.changePane("Main Page"); // si sposta nella pagina principale
			gui.usernameField.setText(""); // resetta username e password
			gui.passwordField.setText("");
			username = user; // setta username loggato
			score(); // aggiorna il punteggio
			ranking(); // aggiorna la classifica
			break;
		case "1": // login fallito
			gui.showError(response.nextToken());
			break;
		default: // errore di comunicazione
			gui.showError("Error comunicating with sever");
			break;
		}
	}

	// recupera la lista degli amici
	private void listFriends() {
		clientTCP.send("FRIENDS\n" + username); // invia la richiesta

		String message = clientTCP.read(); // riceve la risposta

		switch (message.substring(0, 1)) { // codice risposta
		case "0": // richiesta soddisfatta
			Set<String> friends = gson.fromJson(message.substring(2), friendsSetType); // deserializza la lista degli
																						// amici
			// aggiorna la comboBox per la scelta dell'amico
			gui.friendsComboBox.setModel(new DefaultComboBoxModel<String>((String[]) friends.toArray(new String[0])));
			break;
		case "1": // errore durante la richiesta
			gui.showError(message.substring(2));
			break;
		default: // errore di comunicazione
			gui.showError("Error comunicating with sever");
			break;
		}
	}

	// effettua la registrazione
	private void signIn() {
		String user = gui.signUsernameField.getText(); // recupera username, password e password ripetuta
		String password = gui.signPasswordField.getText();
		String repeatedPassword = gui.signRepeatedPasswordField.getText();
		try {
			Registry registry = LocateRegistry.getRegistry(null);
			SignIn stub = (SignIn) registry.lookup("signIn"); // recupera l'oggetto remoto
			try {
				stub.SignInProcedure(user, password, repeatedPassword); // invoca il metodo remoto
			} catch (RemoteException e) { // errore durante registrazione
				gui.showMessage(e.getMessage());
				return;
			}
		} catch (Exception e) {
			System.err.println("Client exception: " + e.toString());
			e.printStackTrace();
			return;
		}
		// registrazione corretta
		gui.showMessage("Sign In successful");
	}

	// invia richiesta per una sfida
	private void requestChallenge() {
		String friend = (String) gui.friendsComboBox.getSelectedItem(); // recupera username dell'amico sfidato

		clientTCP.send("CHALLENGE\n" + username + "\n" + friend); // invia la richiesta

		JDialog loading = gui.loadWaitDialog(); // mostra una dialog di attesa

		SwingWorker<String, Void> worker = new SwingWorker<String, Void>() { // oggetto che attende la risposta dal
																				// server mentre viene mostrata la
																				// dialog
			@Override
			protected String doInBackground() throws InterruptedException {
				return clientTCP.read(); // riceve la risposta
			}

			@Override
			protected void done() { // chiude la dialog quando ha terminato
				loading.dispose();
			}
		};

		worker.execute(); // esegue la lettura e al termine chiude la dialog
		loading.setVisible(true);

		String message = null;
		try {
			message = worker.get(); // risposta
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		StringTokenizer response = new StringTokenizer(message, "\n"); // parsing
		switch (response.nextToken()) {
		case "0": // richiesta accettata
			challenging = true;
			gui.wordLabel.setText(response.nextToken()); // mostra la prima parola
			gui.changePane("Challenge"); // si sposta nella pagina della sfida
			break;
		case "1": // richiesta rifiutata
			gui.showError(response.nextToken());
			break;
		default: // errore di comunicazione
			gui.showError("Error comunicating with sever");
			break;
		}
	}

	// invia la traduzione
	private void sendTranslation() {
		String translation = gui.translationField.getText(); // recupera la traduzione

		clientTCP.send("WORD\n" + username + "\n" + translation); // invia la traduzione al server

		nextWord(); // mostra una nuova parola
	}

	// mostra una nuova parola
	private void nextWord() {
		String message = clientTCP.read(); // riceve la parola dal server
		StringTokenizer response = new StringTokenizer(message, "\n"); // parsing
		switch (response.nextToken()) {
		case "0": // ricevuta parola
			gui.wordLabel.setText(response.nextToken()); // mostra la parola
			gui.translationField.setText(""); // resetta il campo per la traduzione
			break;
		case "1": // lo sfidato ha abbandonato la partita
			gui.showMessage("Your friend left the game. No points gained");
			clientTCP.read(); // ignora il messaggio
			gui.translationField.setText("");
			gui.changePane("Main Page"); // torna alla pagina principale
			challenging = false; // sfida terminata
			ranking(); // aggiorna la classifica
			break;
		case "2": // non ci sono più parole e lo sfidato non ha terminato
			gui.showMessage(response.nextToken());
			gui.translationField.setText("");
			waitResult(); // attende che lo sfidato termini la partita
			break;
		// partita finita per timeout o perchè hai finito
		case "3": // entrambi i giocatori hanno terminato
			showResult(response.nextToken()); // mostra il risultato della partita
			gui.translationField.setText("");
			ranking(); // aggiorna la classifica
			score(); // aggiorna il punteggio
			break;
		case "4": // timeout
			gui.showMessage("Time is out");
			showResult(response.nextToken()); // mostra il risultato della partita
			gui.translationField.setText("");
			clientTCP.read();
			ranking(); // aggiorna la classifica
			score(); // aggiorna il punteggio
			break;
		default: // errore di comunicazione
			gui.showError("Error comunicating with sever");
			break;
		}
	}

	// attende che lo sfidato termini la partita
	private void waitResult() {
		String message = clientTCP.read(); // attende un messaggio

		StringTokenizer response = new StringTokenizer(message, "\n"); // parsing
		switch (response.nextToken()) {
		case "3": // lo sfidato ha terminato
			showResult(response.nextToken()); // mostra il risultato della partita
			ranking(); // aggiorna la classifica
			score(); // aggiorna il punteggio
			break;
		case "4":
			showResult(response.nextToken()); // mostra il risultato della partita
			ranking(); // aggiorna la classifica
			score(); // aggiorna il punteggio
			break;
		case "1": // lo sfidato ha abbandonato la partita
			gui.showMessage("Your friend left the game. No points gained.");
			gui.changePane("Main Page"); // ritorna alla pagina principale
			challenging = false;
			ranking(); // aggiorna classifica
			break;
		default: // errore di comunicazione
			gui.showError("Error comunicating with sever");
			break;
		}

	}

	// mostra il risultato della partita
	private void showResult(String result) {
		String[] results = result.split(":"); // parsing del risultato
		String friend;
		int userAnswers, friendAnswers;
		if (username.equals(results[0])) { // identifica sfidante e sfidato
			userAnswers = Integer.parseInt(results[1]);
			friend = results[2];
			friendAnswers = Integer.parseInt(results[3]);
		} else {
			userAnswers = Integer.parseInt(results[3]);
			friend = results[0];
			friendAnswers = Integer.parseInt(results[1]);
		}
		// calcola punteggi
		int userPoints = Configuration.X * userAnswers - Configuration.Y * (Configuration.K - userAnswers);
		int friendPoints = Configuration.X * friendAnswers - Configuration.Y * (Configuration.K - friendAnswers);
		if (userPoints < 0)
			userPoints = 0;
		if (friendPoints < 0)
			friendPoints = 0;

		String recap = "You guessed " + userAnswers + " translation for a total of " + userPoints
				+ " points. Your friend " + friend + " guessed " + friendAnswers + " translations for a total of "
				+ friendPoints + " points. ";
		String winner;
		if (userAnswers > friendAnswers) // determina vincitore
			winner = "You are the winner! You gain " + Configuration.Z + " bonus points.";
		else if (userAnswers < friendAnswers)
			winner = "Your friend is the winner!";
		else
			winner = "It's a tie! You both receive " + Configuration.Z / 2 + " bonus points.";

		gui.showMessage(recap + winner); // mostra il risultato

		gui.changePane("Main Page"); // torna alla pagina principale
		challenging = false;
	}

	// abbandona la partita
	private void quitChallenge() {
		clientTCP.send("QUIT\n" + username); // invia la richiesta
		clientTCP.read(); // riceve la conferma
		gui.changePane("Main Page"); // torna alla pagina principale
		challenging = false;
		ranking(); // aggiorna la classifica
	}

	// riceve una richiesta di sfida
	public String challengeRequest(String user) {
		if (challenging)
			return "NO"; // se è in un'altra sfida rifiuta la richiesta
		int answer = gui.showConfirm(user + " sent you a challenge request. Accept?"); // mostra dialog di conferma
		if (answer == JOptionPane.YES_OPTION) { // riceve risposta
			return "OK";
		} else {
			return "NO";
		}
	}

	// lo sfidato ha accettato la sfida
	public void acceptedChallenge() {
		String message = clientTCP.read(); // legge la prima parola

		StringTokenizer response = new StringTokenizer(message, "\n"); // parsing
		switch (response.nextToken()) {
		case "0": // ha ricevuto una parola
			challenging = true;
			gui.wordLabel.setText(response.nextToken()); // mostra la parola
			gui.changePane("Challenge"); // mostra la pagina di sfida
			break;
		case "1": // errore durante la richiesta
			gui.showError(response.nextToken());
			break;
		default: // errore di comunicazione
			gui.showError("Error comunicating with sever");
			break;
		}
	}
}
