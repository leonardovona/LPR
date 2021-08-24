/*
 * Leonardo Vona
 * 545042
 */
public class MainClass {

	public static void main(String[] args) {
		int idPaziente = 0; //identifica univocamente un paziente
		int medici = 10; //numero di medici
		
		if(args.length != 3) { //l'input non è valido
			System.out.println("Errore, devi inserire il numero di pazienti in codice bianco, giallo e rosso (in questo ordine)");
			System.exit(1);
		}
		
		GestoreOrtopedia gestore = new GestoreOrtopedia(medici); //crea gestore del reparto di ortopedia
		
		//crea pazienti codice bianco
		for(int i = 0; i < Integer.parseInt(args[0]); i++) {
			(new Paziente(idPaziente++, 0, gestore)).start();
		}
		
		//crea pazienti codice giallo
		for(int i = 0; i < Integer.parseInt(args[1]); i++) {
			(new Paziente(idPaziente++, 1, gestore)).start();
		}
		
		//crea pazienti codice rosso
		for(int i = 0; i < Integer.parseInt(args[2]); i++) {
			(new Paziente(idPaziente++, 2, gestore)).start();
		}
	}
}
