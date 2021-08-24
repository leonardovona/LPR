/*
 * Leonardo Vona
 * 545042
 */
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsonable;

/*
 * Oggetto che rappresenta un conto corrente, con relativo titolare e lista di movimenti.
 * Implementa Jsonable per definire la conversione in json.
 */
public class ContoCorrente implements Jsonable{
	private String titolare;				//titolae del conto corrente
	private List<Movimento> movimenti;		//lista di movimenti relativa al conto corrente
	
	public ContoCorrente() {
		this.titolare = new String();
		this.movimenti = new LinkedList<>();
	}
	
	public ContoCorrente(String titolare, List<Movimento> movimenti) {
		this.titolare = titolare;
		this.movimenti = movimenti;
	}

	@Override
	public String toJson() {
		final StringWriter writable = new StringWriter();
        try {
            this.toJson(writable);			//converte l'oggetto in json
        } catch (final IOException e) {
        }
        return writable.toString();			//converte l'oggetto json in stringa
	}

	//converte this in un oggetto json
	@Override
	public void toJson(Writer writer) throws IOException {
		final JsonObject conto = new JsonObject();
        conto.put("titolare", this.getTitolare());
        final JsonArray movimenti = new JsonArray();
        for(Movimento mov: getMovimenti()){
        	movimenti.add(mov);
        }
        conto.put("movimenti", movimenti);
        conto.toJson(writer);
		
	}

	public String getTitolare() {
		return this.titolare;
	}
	
	public List<Movimento> getMovimenti(){
		return this.movimenti;
	}

	public void setTitolare(String titolare) {
		this.titolare = titolare;
	}

	public void setMovimenti(List<Movimento> movimenti) {
		this.movimenti = movimenti;
	}
	
	
}
