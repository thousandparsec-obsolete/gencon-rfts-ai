package gencon;

public class GenConRunner {

	/**
	 * A very simple harness to run GenCon. 
	 * 
	 * @param args Optional arguments. See README for details.
	 */
	
	public static void main(String[] args) 
	{
		Master master = new Master(args, System.out);
		
		master.run();
	}

}
