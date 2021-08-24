/*
 * Leonardo Vona
 * 545042
 */

//rappresenta un medico del reparto di ortopedia
public class Medico {
	private int id; //identificativo del medico
	private boolean stato; // true libero, false occupato

	public Medico(int id, boolean stato) {
		this.id = id;
		this.stato = stato;
	}

	public boolean isLibero() {
		return stato;
	}

	public void occupa() {
		this.stato = false;
	}
	
	public void libera() {
		this.stato = true;
	}
	
}
