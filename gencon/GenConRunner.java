package gencon;

public class GenConRunner {

	/**
	 * A very simple harness to run the client. 
	 * 
	 * @param args Optional arguments: '-a serverURI' and '-v'. 
	 * To autologin to server as an existing user, type in '-a serverURI'. 
	 * The serverURI must include user info, e.g.: "tp://guest:guest@thousandparsec.net/tp". If none provided, 
	 * user will be manually prompted for user info and server address, without autologin.
	 * To turn on verbose debug mode, type in '-v'.
	 */
	
	public static void main(String[] args) 
	{
		Client genConClient = new Client();
		genConClient.runClient(args);

	}

}
