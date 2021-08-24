/*
 * Leonardo Vona
 * 545042
 */
package wordquizzle.common;

/*
 * Eccezione lanciata da User durante la creazione dell'utente
 */
public class UserException extends Exception {

	public UserException() {
		super();
	}

	public UserException(String s) {
		super(s);
	}

}