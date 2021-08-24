/*
 * Leonardo Vona
 * 545042
 */

import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;

//gestisce i medici e i pazienti del reparto di ortopedia
public class GestoreOrtopedia {
	private final ReentrantLock lock; //lock sulle strutture dati condivise
	private final Condition codRosso; //variabile di condizione per pazienti con codice rosso
	private final Condition[] codGiallo; //variabili di condizione per pazienti con codice giallo, una per ogni medico
	private final Condition codBianco; //variabile di condizione per pazienti con codice bianco
	private Medico[] medici; //insieme dei medici
	private int mediciOccupati; //indica il numero di medici attualmente occupati

	public GestoreOrtopedia(int size) {
		this.lock = new ReentrantLock();
		this.codRosso = lock.newCondition();
		this.codGiallo = new Condition[size];
		this.codBianco = lock.newCondition();
		this.medici = new Medico[size];
		this.mediciOccupati = 0;

		for (int i = 0; i < size; i++) {
			codGiallo[i] = lock.newCondition();
			medici[i] = new Medico(i, true);
		}
	}
	
	//gestisce un paziente in codice bianco, ritorna il medico a cui è stato assegnato
	public int codiceBianco() {
		lock.lock();
		int medico;
		try {
			try {
				/*
				 * Si mette in attesa e libera la lock se (in alternativa):
				 * 		c'è un paziente in codice rosso in attesa
				 * 		tutti i medici sono occupati
				 */
				while (lock.hasWaiters(codRosso) || mediciOccupati == medici.length)
					codBianco.await();
				
			} catch (InterruptedException e) {}
			
			medico = assegnaMedico(); //assegna un medico al paziente
			mediciOccupati++; //incrementa il contatore dei medici occupati
			
		} finally {
			lock.unlock();
		}
		return medico;
	}
	
	//gestisce un paziente in codice giallo ad uno specifico medico
	public void codiceGiallo(int medico) {
		lock.lock();
		try {
			try {
				
				/*
				 * Si mette in attesa e rilascia la lock se:
				 * 		c'è un paziente in codice rosso in attesa
				 * 		il medico richiesto non è libero
				 */
				while (lock.hasWaiters(codRosso) || !medici[medico].isLibero())
					codGiallo[medico].await();
				
			} catch (InterruptedException e) {}
			
			medici[medico].occupa(); //assegna il paziente allo specifico medico
			mediciOccupati++; //aumenta il contatore dei medici occupati
			
		} finally {
			lock.unlock();
		}
	}
	
	//gestisce un paziente in codice rosso
	public void codiceRosso() {
		lock.lock();
		try {
			try {
				
				//se tutti i medici non sono liberi si mette in attesa e libera la risorsa
				while (mediciOccupati != 0)
					codRosso.await();
				
			} catch (InterruptedException e) {}
			
			//occupa tutti i medici
			for (int i = 0; i < medici.length; i++) {
				medici[i].occupa();
			}
			mediciOccupati = medici.length;
			
		} finally {
			lock.unlock();
		}
	}
	
	//termine della visita di un paziente in codice bianco o giallo
	public void terminaSingolo(int medico) {
		lock.lock();
		try {
			
			medici[medico].libera(); //libera lo specifico medico
			
			mediciOccupati--; //decrementa il contatore dei medici occupati
			
			codRosso.signal(); //risveglia un paziente in codice rosso, se c'è
			codGiallo[medico].signal(); //risveglia un paziente in codice giallo che attende il medico specifco, se c'è
			codBianco.signal(); //risveglia un paziente in codice bianco, se c'è
			
		} finally {
			lock.unlock();
		}
	}
	
	//termine della visita di un paziente in codice rosso
	public void terminaCodiceRosso() {
		lock.lock();
		try {
			
			//libera tutti i medici
			for(int i = 0; i < medici.length; i++) {
				medici[i].libera();				
			}
			//azzera il contatore dei medici occupati
			mediciOccupati = 0;
			
			codRosso.signal(); //risveglia un paziente in codice rosso, se c'è
			//risveglia i pazienti in codice giallo per ogni medico, se ci sono
			for(int i = 0; i < codGiallo.length; i++) {
					codGiallo[i].signal();
			}
			codBianco.signal(); //risveglia un paziente in codice bianco, se c'è
		} finally {
			lock.unlock();
		}
	}

	//cerca un medico libero e lo assegna
	public int assegnaMedico() {
		int medico = -1; //-1 se nessun medico è libero
		for (int i = 0; i < medici.length; i++) {
			if (medici[i].isLibero()) { //il medico i è libero
				medici[i].occupa(); //occupa il medico i
				medico = i;
				break;
			}
		}
		return medico;
	}

	public int getNumMedici() {
		return medici.length;
	}
}
