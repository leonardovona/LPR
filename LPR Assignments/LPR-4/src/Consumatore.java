/*
 * Leonardo Vona
 * 545042
 */

import java.io.File;

//thread consumatore che stampa il contenuto di una directory
public class Consumatore extends Thread{
	private MyBlockingQueue<File> queue;	//struttura dati condivisa
	
	public Consumatore(MyBlockingQueue<File> queue) {
		this.queue = queue;
	}
	
	public void run() {
		while(!queue.isTerminated() || !queue.isEmpty()) {	//il produttore non ha terminato o la coda non è vuota
			File dir = queue.dequeue();						//recupera l'elemento in testa alla coda
			String[] files = dir.list();					//recupera gli elementi presenti nella cartella
			if(files != null) {								//si ha accesso agli elementi nella cartella
				for(String f : files) {						//cicla sugli elementi presenti nella cartella
					File file = new File(dir + "/" + f);	
					if(file.isDirectory()) {				//se l'elemento è una directory appende il carattere 'd' prima del suo path
						System.out.printf("d ");
					}
					System.out.println(file);				//stampa il path dell'elemento
				}
			}
		}
	}

}
