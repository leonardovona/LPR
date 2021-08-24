/*
 * Leonardo Vona
 * 545042
 */

package wordquizzle.common;

/*
 * Parametri di configurazione dell'applicazione
 */

public final class Configuration {
	public static final int RMI_REGISTRY_PORT = 1099;
	public static final int TCP_SERVER_PORT = 1919;
	public static final int UDP_CLIENT_PORT = 1919;

	public static final String SERVER_ADDRESS = "localhost";

	public static final String USERS_FILE = "users.json";
	public static final String WORDS_FILE = "words.txt";

	public static final int THREAD_POOL_SIZE = 4;

	public static final int X = 2; // punti assegnati per una risposta corretta
	public static final int Y = 1; // punti decurtati per una risposta errata
	public static final int Z = 3; // punti bonus per la vittoria della partita
	public static final int K = 5; // numero di traduzioni da effettuare
	public static final long T2 = 60 * 1000; // limite di tempo massimo per la sfida
	public static final long T1 = 15 * 1000; // limite di tempo massimo per una singola risposta
}
