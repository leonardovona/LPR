/*
 * Leonardo Vona
 * 545042
 */
package wordquizzle.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

//interfaccia per il metodo remoto
public interface SignIn extends Remote {
	// metodo remoto per la registrazione di un nuovo utente nella piattaforma
	void SignInProcedure(String username, String password, String repeatedPassword) throws RemoteException;
}
