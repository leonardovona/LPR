/*
 * Leonardo Vona
 * 545042
 */

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainClass {

	public static void main(String[] args) {
		int i = 0; //progressivo usato per nominare i task
		int oldSize = 0;//indica il numero di persone presenti nella prima sala all'ultima volta in cui è stata controllata
		Queue<Task> primaSala = new LinkedList<>(); //coda che implementa la prima sala di attesa, di dimensione non prefissata
		int sportelli = 4; //numero di sportelli presenti
		int postiSecondaSala = 8; //numero di posti nella seconda sala (k)
		long maxAttesa = (long) 1.3; //tempo dopo il quale uno sportello viene chiuso (secondi)
		
		/* pool di thread con corePoolSize e maximumPoolSize fissati al numero di sportelli
		 * la workQueue è di tipo linkedBlockingQueue con dimensione massima pari ai posti nella seconda sala
		 */
		ThreadPoolExecutor executor = new ThreadPoolExecutor(sportelli, sportelli, maxAttesa, TimeUnit.SECONDS,
			new LinkedBlockingQueue<Runnable>(postiSecondaSala));
		
		//imposta la possibilità che i thread che fanno parte del core siano eliminati se non gli viene assegnato task entro maxAttesa
		executor.allowCoreThreadTimeOut(true);
		
		//ciclo infinito che simula un flusso continuo di clienti
		while(true) {
			primaSala.add(new Task("" + i)); //simula l'arrivo di un cliente nella prima sala
			try {
				//recupera dalla coda l'elemento in testa senza rimuoverlo e assegna il task al pool di thread
				//(il cliente passa alla seconda sala e se uno sportello è libero esegue la sua richiesta)
				while(!primaSala.isEmpty()) {
					executor.execute(primaSala.peek());
					primaSala.poll();	//il pool di thread ha accettato il task, lo rimuove dalla prima sala
				}
								
			}catch(RejectedExecutionException ex) { //la seconda sala è piena, il cliente non può entrare
				//controlla se il numero di persone in attesa nella prima sala è cambiato
				if(oldSize != primaSala.size()) { 
					oldSize = primaSala.size();
					//stampa le persone in attesa solo se il numero è cambiato
					System.out.println("Persone in attesa nella prima sala: " + oldSize);
				}
				
			}

			try {
				//simula un'attesa tra 500 e 1800 millisecondi tra l'arrivo di due clienti nell'ufficio postale
				Thread.sleep((long)(Math.random()*1300 + 500));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			i++;
		}			
	}

}
