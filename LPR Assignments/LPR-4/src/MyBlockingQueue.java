/*
 * Leonardo Vona
 * 545042
 */

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

//coda thread safe realizzata con una linked list
public class MyBlockingQueue<E>  {
	private LinkedList<E> queue;	//struttura dati effettiva
	private final ReentrantLock lock; //lock sulla struttura dati
	private final Condition empty;	//condition per la sincronizzazione tra thread
	private boolean terminate;		//indica se il thread produttore ha terminato
	
	//inizializza gli attributi di classe
	public MyBlockingQueue(){
		this.lock = new ReentrantLock();
		this.empty = lock.newCondition();
		this.queue = new LinkedList<E>();
		this.terminate = false;
	}
	
	//inserisce un elemento al termine della coda
	public void enqueue(E element) {
		lock.lock();
		
		queue.add(element);
		empty.signal();		//informa eventuali consumatori che la coda non è vuota
		
		lock.unlock();
	}
	
	public E dequeue() {
		E element;
		
		lock.lock();
		
		while(queue.isEmpty()) {	//se la coda è vuota si mette in attesa di una signal e libera la risorsa
			try {
				empty.await();
			} catch (InterruptedException e) {}
		}
		element = queue.poll();		//rimuove l'elemento in testa alla coda
		
		lock.unlock();
		
		return element;
	}
	
	public void terminate() {
		terminate = true;
	}
	
	public boolean isEmpty() {
		return queue.isEmpty();
	}
	
	public boolean isTerminated() {
		return terminate;
	}
}
