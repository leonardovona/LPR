/*
 * Leonardo Vona
 * 545042
 */

package wordquizzle.server;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import wordquizzle.common.Configuration;
import wordquizzle.common.User;
import wordquizzle.common.UserException;

/*
 * Classe che implementa il metodo remoto per la registrazione di un utente
 */
public class ServerRMI implements SignIn {
	private Map<String, User> users; // utenti dell'applicazione
	private Gson gson; // oggetto per la serializzazione su JSON

	public ServerRMI(Map<String, User> users) throws RemoteException, ExportException {
		this.users = users;
		this.gson = new GsonBuilder().setPrettyPrinting().create(); // crea l'oggetto per la serializzazione su JSON

		SignIn stub = (SignIn) UnicastRemoteObject.exportObject(this, 0);
		LocateRegistry.createRegistry(Configuration.RMI_REGISTRY_PORT);
		Registry registry = LocateRegistry.getRegistry();
		registry.rebind("signIn", stub); // registra this come implementatore del metodo signIn
		System.out.println("RMI Server listening on port " + Configuration.RMI_REGISTRY_PORT);
	}

	/*
	 * Registra un nuovo utente nella piattaforma
	 */
	@Override
	public void SignInProcedure(String username, String password, String repeatedPassword) throws RemoteException {
		User user;
		try {
			user = new User(username, password, repeatedPassword);
		} catch (UserException e) {
			throw new RemoteException(e.getMessage());
		} // crea un utente verificando i dati inseriti
		if (users.putIfAbsent(username, user) != null) // aggiunge l'utente alla piattaforma, se non è già presente
			throw new RemoteException("Username already present");
		save(); // memorizza i cambiamenti su file
	}

	// serializza su file JSON
	private void save() {
		String json = gson.toJson(users); // converte l'oggetto contenente gli utenti in JSON
		try (Writer writer = new FileWriter(Configuration.USERS_FILE)) {
			writer.write(json); // serializza su file
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
