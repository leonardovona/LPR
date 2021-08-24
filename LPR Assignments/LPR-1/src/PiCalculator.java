/*
 * Leonardo Vona
 * 545042
 */

//task che calcola un'approssimazione di pigreco con una differenza da Math.PI di accuracy
public class PiCalculator implements Runnable{
	private double accuracy;	 //accuracy in decimale
	private double pi;			//tempo massimo di attesa in millisecondi
	
	public PiCalculator(double accuracy) {
		this.accuracy = accuracy; //avvalora l'attributo accuracy con il valore ricevuto
		this.pi = 4; //inizializzazione valore di pi greco
	}

	@Override
	public void run() { 
		int i = 3; //indice all'interno della serie di Gregory-Leibniz
		boolean pari = true; //quando è pari deve sottrarre. sommare altrimenti
		 //il ciclo termina quando si è raggiunta accuracy o il thread riceve un'interruzione
		while(Math.abs(pi - Math.PI) > accuracy && !Thread.interrupted()) {
			if(pari) {
				pi -= 4/(double)i;
			}else {
				pi += 4/(double)i;
			}
			pari = !pari; //inverte il valore di pari
			i += 2;
		}
	}
	
	//getter per il valore approssimato di pi
	public double getPi() {
		return pi;
	}
}	