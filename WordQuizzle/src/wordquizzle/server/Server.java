/*
 * Leonardo Vona
 * 545042
 */

package wordquizzle.server;

import java.io.FileReader;
import java.io.IOException;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import wordquizzle.common.Configuration;
import wordquizzle.common.User;

/*
 * Classe principale del server
 *
 */

public class Server {

	public static void main(String args[]) {
		Map<String, User> users; // Map contenente gli utenti dell'applicazione
		users = retrieveUsers(); // recupera gli utenti da file .json
		try {
			new ServerRMI(users); // inizializza l'oggetto per la gestione di RMI
		} catch (RemoteException e) {
			System.err.println(e.getMessage());
			return;
		}
		new ServerTCP(users); // inizializza e avvia il server TCP

	}

	// recupera gli utenti da file json tramite Gson
	private static Map<String, User> retrieveUsers() {
		Gson gson = new Gson(); // imposta l'oggetto Gson per la lettura del file JSON
		Type userMapType = new TypeToken<Map<String, User>>() {
		}.getType(); // Tipo dell'oggetto contenente gli utenti
		Map<String, User> users = null;
		try {
			users = gson.fromJson(new FileReader(Configuration.USERS_FILE), userMapType); // recupera gli utenti dal
																							// file
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (users == null) // non c'era nessun utente nel file
			users = new HashMap<>();
		return users;
	}
}