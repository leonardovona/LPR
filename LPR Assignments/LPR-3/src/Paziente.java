/*
 * Leonardo Vona
 * 545042
 */

//oggetto che rappresenta un paziente del reparto di ortopedia
public class Paziente extends Thread{
	private int id; //identificativo del paziente
	private int urgenza; //codice di urgenza: 0 bianco, 1 giallo, 2 rosso
	private GestoreOrtopedia ortopedia; //gestore del reparto di ortopedia, oggetto condiviso
	private int numVisite; //numero di visite che effettua il paziente (k)
	
	
	public Paziente(int id, int urgenza, GestoreOrtopedia ortopedia) {
		this.id = id;
		this.urgenza = urgenza;
		this.ortopedia = ortopedia;
		this.numVisite = (int) (Math.random() * 5) + 1; //+ 1 garantisce che sia fatta almeno una visita
	}
	
	public void run() {
		switch(urgenza) {	//discrimina rispetto al codice di urgenza del paziente
		case 0:
			for(int i = 0; i < numVisite; i++) { //effettua numVisite visite
				
				System.out.println("Paziente " + id + " visita " + (i+1) + " codice bianco: in attesa");
				
				//informa dell'arrivo di un paziente con codice bianco, riceve il medico a cui è stato assegnato
				int medico = ortopedia.codiceBianco();
				
				System.out.println("Paziente " + id + " visita " + (i+1) + " codice bianco: visita in corso da medico " + medico);
				
				//simula tempo di visita
				try {
					Thread.sleep((long) (Math.random() * 2000));
				} catch (InterruptedException e) {}
				
				//informa del termine della visita
				ortopedia.terminaSingolo(medico);
				
				System.out.println("Paziente " + id + " visita " + (i+1) + " codice bianco: visitato");
				
				//simula attesa tra due visite dello stesso paziente
				try {
					Thread.sleep((long) (Math.random() * 2000));
				} catch (InterruptedException e) {}
			}
			break;
		case 1:
			//sceglie un medico da cui essere visitato
			int medico = (int) (Math.random() * ortopedia.getNumMedici());
		
			for(int i = 0; i < numVisite; i++) {
				System.out.println("Paziente " + id + " visita " + (i+1) + " codice giallo: in attesa");
				
				//richiede visita con medico specifico
				ortopedia.codiceGiallo(medico);
				
				System.out.println("Paziente " + id + " visita " + (i+1) + " codice giallo: visita in corso");
				
				try {
					Thread.sleep((long) (Math.random() * 3000));
				} catch (InterruptedException e) {}
				
				ortopedia.terminaSingolo(medico);
				
				System.out.println("Paziente " + id + " visita " + (i+1) + " codice giallo: visitato");
				
				try {
					Thread.sleep((long) (Math.random() * 3000));
				} catch (InterruptedException e) {}
			}
			break;
		case 2:
			for(int i = 0; i < numVisite; i++) {
				
				System.out.println("Paziente " + id + " visita " + (i+1) + " codice rosso: in attesa");
				
				ortopedia.codiceRosso();
				
				System.out.println("Paziente " + id + " visita " + (i+1) + " codice rosso: visita in corso");
				
				try {
					Thread.sleep((long) (Math.random() * 2500));
				} catch (InterruptedException e) {}
				
				ortopedia.terminaCodiceRosso();
				
				System.out.println("Paziente " + id + " visita " + (i+1) + " codice rosso: visitato");
				
				try {
					Thread.sleep((long) (Math.random() * 5000));
				} catch (InterruptedException e) {}
			}
			break;
		}
	}
	
}
