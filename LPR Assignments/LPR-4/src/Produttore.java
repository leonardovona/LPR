/*
 * Leonardo Vona
 * 545042
 */

import java.io.File;
import java.io.IOException;

//thread che visita la directory principale e ricorsivamente le directory annidate
public class Produttore extends Thread{
	private File mainDirectory;				//directory principale
	private MyBlockingQueue<File> queue;	//struttura dati condivisa
	
	public Produttore(String file, MyBlockingQueue<File> queue) throws IOException{
		mainDirectory = new File(file);
		this.queue = queue;
		//se  file path non è una directory restituisce una IOException
		if(!mainDirectory.isDirectory()) {		
			throw new IOException("Errore di I/O, il filepath inserito non è una directory o non si possiedono i diritti per accedervi");
		}
	}
	
	public void run() {
		System.out.println("d " + mainDirectory);	//stampa il path della directory principale
		queue.enqueue(mainDirectory);				//inserisce la directory principale nella coda
		visit(mainDirectory);						//visita ricorsivamente le sub directory
		queue.terminate();							//segnala che ha terminato la visita
		
	}

	private void visit(File dir) {
		String[] files = dir.list();				//recupera l'elenco degli elementi presenti nella directory
		if(files == null) return;					//non si hanno i diritti di accesso alla directory
		for(String f : files) {						//itera sugli elementi nella directory
			File file = new File(dir + "/" + f);	//associa gli elementi a dei file
			if(file.isDirectory()) {				//l'elemento è una directory
				queue.enqueue(file);				//inserisce la sub directory nella coda
				visit(file);						//visita ricorsivamente la sub directory
			}
		}
	}
}

