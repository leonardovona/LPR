/*
 * Leonardo Vona 
 * 545042
 */
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/*
 * Task che conta le occorrenze di ogni causale per un dato conto corrente
 */
public class ContaOccorrenze implements Runnable {
	private ContoCorrente conto;						//conto corrente
	private Map<String, Integer> occorrenze;			//occorrenze per ogni causale per il conto dato
	private Map<String, Integer> globalOccorrenze;		//oggetto condiviso che contiene il totale delle occorrenze per ogni causale in ogni conto

	public ContaOccorrenze(ContoCorrente conto, Map<String, Integer> globalOccorrenze) {
		this.conto = conto;
		this.occorrenze = new HashMap<>();
		this.globalOccorrenze = globalOccorrenze;
	}

	@Override
	public void run() {
		//per ogni movimento del conto
		for (Movimento m : conto.getMovimenti()) {
			String causale = m.getCausale();							//recupera la causale del movimento
			if (occorrenze.containsKey(causale)) {						//la Map contiene già la causale
				occorrenze.put(causale, occorrenze.get(causale) + 1);	//incrementa le occorrenze della causale
			} else {													//la Map non contiene la causale
				occorrenze.put(causale, 1);								//inserisce la causale e imposta il numero di occorrenze a uno
			}
		}
		
		//somma le occorrenze delle causali del conto locale a quelle globali
		synchronized (globalOccorrenze) {													//blocco thread safe su oggetto condiviso
			Set<String> causali = occorrenze.keySet();										//recupera le causali
			for (String c : causali) {														//per ogni causale
				if (globalOccorrenze.containsKey(c)) {										//la causale è presente nella map globale
					globalOccorrenze.put(c, occorrenze.get(c) + globalOccorrenze.get(c));	//somma le occorrenze locali a quelle globali
				} else {																	//la causale non è presente nella map globale
					globalOccorrenze.put(c, occorrenze.get(c));								//imposta le occorrenze locali come globali
				}
			}
		}
	}
}
