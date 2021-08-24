/*
 * Leonardo Vona
 * 545042
 */
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import com.github.cliftonlabs.json_simple.Jsoner;

/*
 * Crea dei conti correnti casuali, li serializza e li scrive su file.
 * Attiva thread lettore per il recupero dei conti correnti da file
 */
public class MainClass {
	private static final int CLIENTI = 50; 					// numero di clienti
	private static final int MAXMOVIMENTI = 300; 			// numero massimo di movimenti per ogni cliente
	private static final String JSONFILE = "conti.json"; 	// file json
	private static Calendar calendar; 						// calendario per la gestione delle date
	private static Date inizio; 							// limite inferiore per la data del movimento
	private static Date fine; 								// limite superiore per la data del movimento
	// formatta la data nel formato italiano
	private static SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
	// possibili causali
	private static final String[] causali = new String[] { "bonifico", "accredito", "bollettino", "F24",
			"pagobancomat" };

	public static void main(String[] args) {
		// imposta i limiti inferiore e superiore per la data dei movimenti
		calendar = Calendar.getInstance();
		calendar.set(2017, 11, 01);
		inizio = calendar.getTime();
		calendar.set(2019, 10, 31);
		fine = calendar.getTime();

		Map<String, Integer> occorrenze = new HashMap<>(); 			// insieme di coppie (causale, numOccorrenze)

		System.out.println("Creazione conti");
		List<ContoCorrente> conti = creaConti(); 					// creazione randomica degli oggetti contoCorrente
		writeToFile(conti); 										// scrittura su file json degli oggetti contoCorrente

		JsonReader reader = new JsonReader(JSONFILE, occorrenze); 	// task per la lettura dei conticorrenti da file json
		Thread thread = new Thread(reader); 						// thread associato al task reader
		thread.start(); 											// avvia il thread

		try {
			thread.join(); 											// attende la terminazione del thread
		} catch (InterruptedException e) {
		}

		int totMovimentiLetti = 0; 									// numero totale di movimenti letti da file
		for (String causale : occorrenze.keySet()) { 				// itera sulle causali
			totMovimentiLetti += occorrenze.get(causale); 			// aggiunge i movimenti al totale
			// per ogni causale recupera il numero di movimenti e lo stampa
			System.out.println("Numero di " + causale + ": " + occorrenze.get(causale));
		}

		System.out.println("\nNumero totale di movimenti letti: " + totMovimentiLetti);

	}

	// crea una lista di conti correnti in modo casuale
	private static List<ContoCorrente> creaConti() {
		List<ContoCorrente> conti = new LinkedList<>(); 				// lista di conti corrente da restituire
		int totaleMovimenti = 0;										// numero di movimenti creati
		// ripete il ciclo per il numero di clienti che si vuole creare
		for (int i = 0; i < CLIENTI; i++) {
			String nome = "cliente " + i; 								// assegna un nome al cliente
			List<Movimento> movimenti = new LinkedList<>(); 			// lista dei movimenti associata al conto
			// calcola numero casuale di movimenti tra 10 e MAXMOVIMENTI
			int numMovimenti = ThreadLocalRandom.current().nextInt(10, MAXMOVIMENTI);
			totaleMovimenti += numMovimenti; 							// somma al totale dei movimenti
			// ripete il ciclo per il numero di movimenti che si vuole creare per un dato conto
			for (int j = 0; j < numMovimenti; j++) {
				// crea un movimento con data e causale random
				Movimento mov = new Movimento(getRandomDate(), getRandomCausale());
				movimenti.add(mov); 									// aggiunge il movimento alla lista
			}

			ContoCorrente cc = new ContoCorrente(nome, movimenti); 		// conto corrente da aggiungere alla lista
			conti.add(cc); 												// aggiunge il conto corrente alla lista
		}

		System.out.println("Numero totale di movimenti creati: " + totaleMovimenti);
		return conti;
	}

	// genera una causale random tra quelle possibili
	private static String getRandomCausale() {
		int i = ThreadLocalRandom.current().nextInt(0, 5);
		return causali[i];

	}

	// genera una data casuale in formato italiano nell'intervallo previsto
	private static String getRandomDate() {
		Date data = new Date(ThreadLocalRandom.current().nextLong(inizio.getTime(), fine.getTime()));
		return format.format(data);
	}

	// serializza tramite json e scrive su file con NIO
	private static void writeToFile(List<ContoCorrente> conti) {
		System.out.println("Serializzazione json");
		String json = Jsoner.serialize(conti); 					// serializza la lista di conti corrente
		json = Jsoner.prettyPrint(json);
		byte[] bytes = json.getBytes(); 						// converte la stringa in array di byte

		System.out.println("Scrittura su file");
		// crea buffer di lunghezza pari alla lista di conti correnti serializzata e
		// convertita in byte
		ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);

		FileChannel outChannel = null;
		try {
			// channel per la scrittura su JSONFILE
			outChannel = FileChannel.open(Paths.get(JSONFILE), StandardOpenOption.WRITE);

			outChannel.truncate(0); 							// elimina il contenuto del file
			buffer.put(bytes); 									// inserisce nel buffer i conti correnti
			buffer.flip(); 										// prepara per la scrittura del channel dal buffer
			outChannel.write(buffer); 							// channel scrive il contenuto di buffer su file

			outChannel.close(); 								// chiude il canale
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}