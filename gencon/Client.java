package gencon;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.concurrent.Future;

import net.thousandparsec.netlib.*;
import net.thousandparsec.netlib.tp03.*;

public class Client extends TP03Visitor 
{
	String connectionString;
	Connection<TP03Visitor> conn;
	
	Client() {}
	
	void init(String conStr)
	{
		this.connectionString = conStr;
	}
	
	/**
	 * 
	 * Runs the client.
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 * @throws TPException
	 * 
	 */
	void run() throws UnknownHostException, IOException, URISyntaxException, InterruptedException, TPException
	{
		establishConnection();
		
		conn.close();
	}
	
/**
 * Establishes a connection with the server.
 * 
 * @throws UnknownHostException
 * @throws IOException
 * @throws URISyntaxException
 * @throws InterruptedException
 * @throws TPException
 */
	private void establishConnection() throws UnknownHostException, IOException, URISyntaxException, InterruptedException, TPException
	{
		TP03Decoder decoder = new TP03Decoder();
		conn = decoder.makeConnection(new URI(connectionString), true, new TP03Visitor(false));
		conn.addConnectionListener(new DefaultConnectionListener<TP03Visitor>());
	}
	
}
