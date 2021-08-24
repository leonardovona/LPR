/*
 * Leonardo Vona
 * 545042
 */
package wordquizzle.common;

/*
 * Elemento della classifica, composto da una coppia <username, punteggio>
 */
public class RankEntry implements Comparable<RankEntry> {
	private String username; // utente
	private int points; // punteggio

	public RankEntry(String username, int points) {
		this.username = username;
		this.points = points;
	}

	// ridefinisce il metodo compareTo per ordinare correttamente la classifica
	@Override
	public int compareTo(RankEntry entry) {
		return this.points - entry.getPoints();
	}

	public String getUsername() {
		return username;
	}

	public int getPoints() {
		return points;
	}

}
