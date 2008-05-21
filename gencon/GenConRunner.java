package gencon;

public class GenConRunner {

	/**
	 * Very simple harness to run the client. 
	 * @param args Optional: '-a serverURI' to autologin to server. The serverURI must include user info, 
	 * e.g.: "tp://guest:guest@thousandparsec.net/tp". If none provided, user will be manually prompted for 
	 * user info and server address, without autologin.
	 */
	
	public static void main(String[] args) 
	{
		Client genConClient = new Client();
		genConClient.runClient(args);

	}

}
