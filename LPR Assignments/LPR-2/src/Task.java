/*
 * Leonardo Vona 
 * 545042
 */

//cliente dell'ufficio postale
public class Task implements Runnable{
	private String name; //nome del cliente
	
	public Task(String name) {
		this.name = name;
	}
	
	@Override
	public void run() {
		//calcola un tempo random che necessita per eseguire la richiesta
		Long duration = (long) (Math.random()*10);
		//informa che uno sportello sta servendo il cliente
		System.out.println(Thread.currentThread().getName() + ": Servo cliente " + name + " per " + duration + " secondi");
		try {
			//esegue la richiesta
			Thread.sleep(duration*1000);
		}catch(InterruptedException ex) {
			ex.printStackTrace();
		}
	}
}
