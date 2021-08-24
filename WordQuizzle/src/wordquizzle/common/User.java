/*
 * Leonardo Vona
 * 545042
 */
package wordquizzle.common;

import java.math.BigInteger;
import java.nio.channels.SelectionKey;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.HashSet;
import java.util.Set;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/*
 * Oggetto che rappresenta un utente della piattaforma
 */
public class User {
	private String username; // nome utente
	private String password; // la password memorizzata è composta dal numero di iterazioni da effettuare per
								// ottenere l'hash, il salt e l'hash effettivo
	private Set<String> friends; // insieme degli amici dell'utente
	private int points; // punteggio dell'utente
	private transient boolean logged; // indica se l'utente è loggato o meno (non viene serializzato)
	private transient SelectionKey key; // chiave associata all'utente (non viene serializzata)

	public User(String username, String password, String repeatedPassword) throws UserException {
		if (username == null || username.isEmpty()) // username non valido
			throw new UserException("Username not set");
		if (password == null || password.length() < 8) // password non valida
			throw new UserException("Unvalid password");
		if (repeatedPassword == null || !repeatedPassword.equals(password)) // le password non coincidono
			throw new UserException("Password does not match");
		this.username = username;
		this.password = hash(password); // memorizza l'hash della password
		this.friends = new HashSet<>();
	}
	
	// costruttore utilizzato da Gson per la deserializzazione
	public User(String username, String password, Set<String> friends, int points) throws UserException {
		if (username == null || username.isEmpty())
			throw new UserException("Username not set");
		if (password == null || password.length() < 8)
			throw new UserException("Unvalid password");
		if (friends == null)
			throw new UserException("Unvalid friends list");
		if (points <= 0)
			throw new UserException("Unvalid points");
		this.username = username;
		this.password = password;
		this.friends = friends;
		this.points = points;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public String getUsername() {
		return username;
	}

	public Set<String> getFriends() {
		return friends;
	}

	public boolean isFriend(String username) {
		if (username == null)
			return false;
		return friends.contains(username);
	}

	public boolean isLogged() {
		return logged;
	}

	public void login(SelectionKey key) {
		if (key == null)
			throw new NullPointerException("Key can't be null");
		this.key = key;
		logged = true;
	}

	public void logout() {
		this.key = null;
		logged = false;
	}

	public SelectionKey getKey() {
		return key;
	}

	public void addPoints(int points) {
		if (points > 0)
			this.points += points;
	}

	// Aggiunge un amico all'utente
	public void addFriend(User friend) throws FriendException {
		String username = friend.getUsername();
		if (this.equals(friend)) // l'utente sta cercando di aggiungersi da solo come amico
			throw new FriendException("You can't add yourself to your friends");
		if (!friends.add(username)) // verifica che friend non sia già amico
			throw new FriendException(friend.getUsername() + " is already a friend");
	}

	@Override
	public boolean equals(Object obj) {
		// due utenti sono uguali se hanno lo stesso username
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "User [username=" + username + ", password=" + password + ", friends=" + friends + ", points=" + points
				+ ", logged=" + logged + ", key=" + key + "]";
	}

	// verifica se la password in input è corretta
	public boolean passwordMatch(String password) {
		String[] parts = this.password.split(":"); // suddivide la password memorizzata per recuperare i campi
		int iterations = Integer.parseInt(parts[0]);
		byte[] salt = fromHex(parts[1]);
		byte[] hash = fromHex(parts[2]);
		byte[] testHash = null;

		PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, hash.length * 8); // oggetto per
																										// l'hashing
		SecretKeyFactory skf;
		try {
			skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			testHash = skf.generateSecret(spec).getEncoded(); // crea hash
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}

		// verifica uguaglianza
		int diff = hash.length ^ testHash.length;
		for (int i = 0; i < hash.length && i < testHash.length; i++) {
			diff |= hash[i] ^ testHash[i];
		}
		return diff == 0;
	}

	// crea l'hash della password data in input
	private String hash(String password) {
		int iterations = 256; // numero di iterazioni da effettuare per creare l'hash
		char[] chars = password.toCharArray();
		byte[] salt = getSalt(); // salt utilizzato per la creazione dell'hash
		byte[] hash = null;

		PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 64 * 8); // oggetto per creazione hash
		SecretKeyFactory skf;
		try {
			skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			hash = skf.generateSecret(spec).getEncoded(); // crea l'hash
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return iterations + ":" + toHex(salt) + ":" + toHex(hash);
	}

	// genera un salt per l'hashing della password
	private byte[] getSalt() {
		SecureRandom sr = null;
		try {
			sr = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		byte[] salt = new byte[16];
		sr.nextBytes(salt);
		return salt;
	}

	// converte l'hash da array di byte a stringa
	private String toHex(byte[] array) {
		BigInteger bi = new BigInteger(1, array);
		String hex = bi.toString(16);
		int paddingLength = (array.length * 2) - hex.length();
		if (paddingLength > 0) {
			return String.format("%0" + paddingLength + "d", 0) + hex;
		} else {
			return hex;
		}
	}

	// converte l'hash da stringa ad array di byte
	private byte[] fromHex(String hex) {
		byte[] bytes = new byte[hex.length() / 2];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
		}
		return bytes;
	}
}
