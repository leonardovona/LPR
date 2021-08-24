/*
 * Leonardo Vona
 * 545042
 */

import java.io.File;
import java.io.IOException;

public class MainClass {
	//filepath (String) filepath della directory di partenza
	private static final int k = 5;		//numero di thread consumatori
	
	public static void main(String[] args) {
		
		if(args.length != 1) { //l'input non è valido
			System.out.println("Errore, devi inserire un filepath che individua una directory");
			System.exit(1);
		}
		
		MyBlockingQueue<File> queue = new MyBlockingQueue<>();	//coda thread safe realizzata con linked list, condivisa tra i thread
		
		try {
			(new Produttore(args[0], queue)).start();	//inizializza e avvia il produttore
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for(int i = 0; i < k; i++) {	//inizializza e avvia i consumatori
			(new Consumatore(queue)).start();
		}		
	}

}
