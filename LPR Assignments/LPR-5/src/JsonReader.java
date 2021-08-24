/*
 * Leonardo Vona
 * 545042
 */
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

/*
 * Recupera da file e deserializza una lista di conti correnti.
 * Passa ad un pool di thread un conto alla volta per il conteggio delle causali
 */
public class JsonReader implements Runnable {
	private String filename;					//nome del file json
	private Map<String, Integer> occorrenze;	//insieme di coppie (causale, numeroOccorrenze)
	
	public JsonReader(String filename, Map<String, Integer> occorrenze) {
		this.filename = filename;
		this.occorrenze = occorrenze;
	}

	@Override
	public void run() {
		System.out.println("Lettura da file");
		JsonArray jArray = readJson();									//legge da file json
		System.out.println("Deserializzazione json");
		List<ContoCorrente> conti = parseJson(jArray);					//converte oggetto json in lista di conti correnti
		
		System.out.println("Conteggio movimenti");
		/*
		 * crea thread pool con numero di thread nel core pari a 4, numero massimo di thread a 10,
		 * tempo di attesa prima della terminazione di un thread senza task di 5 secondi e
		 * coda implementata tramite LinkedBlockingQueue
		 */
		ThreadPoolExecutor executor = new ThreadPoolExecutor(4, 10, 5L, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>());
		
		for(ContoCorrente conto: conti) { 
			executor.execute(new ContaOccorrenze(conto, occorrenze)); 	//crea un task per ogni conto
		} 
		
		executor.shutdown(); 											//imposta al pool di thread di terminare dopo il completamento di tutti i task in attesa
		
		try {
			executor.awaitTermination(20L, TimeUnit.SECONDS);			//aspetta la terminazione del pool di thread per massimo 20 secondi
		} catch (InterruptedException e) { }
		
		// join dei thread nel threadpool
	}
	
	//legge da file json tramite NIO
	private JsonArray readJson() {
		FileChannel inChannel = null;								//channel per la lettura del file
		ByteBuffer buffer = null;
		JsonArray cc = null;										//array di oggetti json recuperati da file
		try {
			//apre canale in lettura su JSONFILE
			inChannel = FileChannel.open(Paths.get(filename), StandardOpenOption.READ);	

			buffer = ByteBuffer.allocate((int) inChannel.size());	//alloca un buffer di dimensione pari alla lunghezza del file
			inChannel.read(buffer);									//legge dal canale e memorizza sul buffer

			inChannel.close();										//chiude il canale

			String s = new String(buffer.array());					//converte il buffer visto come array di byte in una stringa
			cc = (JsonArray) Jsoner.deserialize(s);					//deserializza la stringa tramite json
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JsonException e) {
			e.printStackTrace();
		}
		return cc;	//restituisce array di oggetti json che rappresentano conti correnti
	}

	//converte l'array di oggetti json in una lista di conti correnti
	private List<ContoCorrente> parseJson(JsonArray jArray) {
		List<ContoCorrente> conti = new LinkedList<>();						//lista di conti correnti da restituire
		for (Object obj : jArray) {											//per ogni elemento dello json array
			JsonObject jConto = (JsonObject) obj;							//considera l'oggetto come uno JsonObject
			ContoCorrente c = new ContoCorrente();							//oggetto conto corrente destinazione per la conversione
			c.setTitolare((String) jConto.get("nome"));						//converti il nome del titolare
			JsonArray jMovimenti = (JsonArray) jConto.get("movimenti");		//recupera lista dei movimenti come json array
			List<Movimento> movimenti = new LinkedList<>();					//lista dei movimenti destinazione
			for (Object mov : jMovimenti) {									//per ogni movimento
				JsonObject jMovimento = (JsonObject) mov;					//considera l'oggetto come uno JsonObject
				Movimento m = new Movimento();								//movimento destinazione della conversione
				m.setData((String) jMovimento.get("data"));					//converte la data
				m.setCausale((String) jMovimento.get("causale"));			//converte la causale
				movimenti.add(m);											//aggiunge il movimento alla lista di movimenti
			}
			c.setMovimenti(movimenti);										//aggiunge la lista di movimenti al conto corrente
			conti.add(c);													//aggiunge il conto corrente alla lista dei conti
		}
		return conti;														//restituisce la lista dei conti correnti
	}
}
