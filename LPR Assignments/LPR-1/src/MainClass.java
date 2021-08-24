/*
 * Leonardo Vona
 * 545042
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainClass {

	public static void main(String[] args) {
		double accuracy = 0;	//accuracy in decimale 
		int max_wait = 0;		//tempo massimo di attesa in millisecondi
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in)); //Reader per lettura input da tastiera
		try {
			//acquisizione accuracy
			System.out.println("Accuracy: ");
			accuracy = Double.parseDouble(reader.readLine());
			//acquisizione max_wait
			System.out.println("Attesa massima (ms): ");
			max_wait = Integer.parseInt(reader.readLine());
		} catch (NumberFormatException | IOException e) {	//gestione eccezioni valori non numerici ed erriori I/O
			System.out.println("Errore: il valore inserito non è un numero");
			return;
		}
		
		PiCalculator piCalc = new PiCalculator(accuracy); //task che calcola l'approssimazione di pi greco
		Thread thread = new Thread(piCalc); //creo un thread associato al task piCalc
		thread.start(); //avvio il thread 
		
		try {
			thread.join(max_wait); //attende al massimo max_wait che il thread termini
		} catch (InterruptedException e) { //gestione exception dovuta ad un'interruzione ricevuta
			e.printStackTrace();
		}
		
		if(thread.isAlive()){ //il thread non ha finito di calcolare
			thread.interrupt(); //interrompe il thread
			System.out.println("Thread interrotto");
		}else { //il thread ha finito di calcolare
			System.out.println("Il valore calcolato di pi gredo è: " + piCalc.getPi());
		}
	}
}
