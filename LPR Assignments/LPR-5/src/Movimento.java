/*
 * Leonardo Vona
 * 545042
 */
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsonable;

/*
 * Rappresenta un movimento su un conto corrente.
 * L'oggetto implementa Jsonable per dofinire come essere convertito in json.
 */
public class Movimento implements Jsonable{
	private String data;			//data del movimento
	private String causale;			//causale del movimento
	
	public Movimento() {
		data = new String();
		causale = new String();
	}
	
	public Movimento(String data, String causale) {
		this.data = data;
		this.causale = causale;
	}

	@Override
	public String toJson() {
		final StringWriter writable = new StringWriter();
        try {
            this.toJson(writable);	//converte l'oggetto in json
        } catch (final IOException e) {
        }
        return writable.toString();	//converte l'oggetto json in stringa
	}

	//converte l'oggetto in un oggetto json, impostando i vari attributi
	@Override
	public void toJson(Writer writer) throws IOException {
		final JsonObject jsonobj = new JsonObject();
        jsonobj.put("data", this.getData());
        jsonobj.put("causale", causale);
        jsonobj.toJson(writer);
	}
	
	public String getData() {
		return data;
	}
	
	public String getCausale() {
		return causale;
	}

	public void setData(String data) {
		this.data = data;
	}

	public void setCausale(String causale) {
		this.causale = causale;
	}

	
}
