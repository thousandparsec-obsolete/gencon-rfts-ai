package gencon;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.Future;

import net.thousandparsec.netlib.*;
import net.thousandparsec.netlib.tp03.*;

public class Client
{
	//maintanence
	private String URIString;
	private Connection<TP03Visitor> conn;
	private PrintStream stout = System.out; 
	private Scanner stin = new Scanner(System.in);
	
	//game-related
	private int difficulty;
	
	/**
	 * Runner method for the client.
	 * 
	 * @param args [0] -c : connect to a URI specified in [1]. Else if none given: proceed normally.
	 */
	public static void main(String[] args)
	{
		System.out.println("Genetic Conquest: An AI Client for Thousand Parsec : RFTS ruleset.\n");
		
		if (args.length > 0 && args[0].equals("-c"))
			new Client(args[1]);
		else if (args.length == 0)
			new Client();
		else
		{
			System.out.print("Input error. Try again.");
			System.exit(-1);
		}
	}
	
	/**
	 * Starts a client without specified URI. Manual entry of URI components (server address, username, password)
	 *
	 */
	Client()
	{
		init();
	}
	
	/**
	 * Starts a client with specified URI. For testing and autoconnect.
	 * @param args same as in {@link main}
	 */
	Client(String URI) 
	{
		init(URI);
	}
	
	/**
	 * initializes client without specified URI string.
	 *
	 */
	private void init()
	{
 		stout.print("Enter the address of the server : ");
		String server = stin.next();
		
		stout.println("Create new account or use existing? (enter: 'n' or 'e'");
		
		String login = stin.next();
		if (login.equals("e"))
		{
			setURIfromStdinput(server);
		}
		else if (login.equals("n"))
		{
			//the first string in user[] is username, the second is password.
			String[] user = createNewAccount(server);
			setURI(user[0], user[1], server); 
		}

		//run client
		run();
	}
	
	/**
	 * Initializes client with specified URI string.
	 * @param URI the string that specifies the address of the server, the username, and the password
	 */
	private void init(String URI)
	{
		this.URIString = URI;
		
		//run client
		run();
	}
	
	private void setURIfromStdinput(String server)
	{
		stout.print("Enter username : ");
		String usrname = stin.next();
		stout.print("Enter password : ");
		String pwd = stin.next();
		
		//the URI string is formatted thus:  
		this.URIString = "tp://" + usrname + ":" + pwd + "@" + server;
	}
	
	private void setURI(String usrname, String pwd, String server)
	{
		this.URIString = "tp://" + usrname + ":" + pwd + "@" + server;
	}
	
	/**
	 * 
	 * @return String[] : index 0 is username, index 1 is password.
	 */
	private String[] createNewAccount(String server)
	{
		return null;
	}
	
	/**
	 * Runs the client.
	 */
	private void run()
	{
		//first establish a connection with the server
		establishConnection();
		
		//set to default:
		setDifficulty(5, false);
		
		//execute command-line orders, until encountering control character 'q'
		stout.println("Enter command. 'list' shows all available commands.");	
		
		while(true == true)
		{
			stout.print("Enter command > ");
			String command = stin.nextLine();
			
			if (command.equals("q")){
				exit("Manual exit from client.");
			} 
			else if (command.equals("list")){
				printCommands();
			}
			else if (command.equals("start")){
				startPlay();
			}
			else if (command.length() == 6 && 
					command.substring(0, 4).equals("diff") && 
					command.substring(5, 6).matches("[1-9]")){
				try
				{
					setDifficulty(new Integer(command.substring(5,6)).intValue(), true);
				}
				catch (Exception e)
				{
					printInvalid();
				}
			}
			
			else {
				printInvalid();
			}
		}
	}
	
	/**
	 * Prints out list of accepted commands.
	 */
	private void printCommands()
	{
		stout.println("\n> The list of available commands <");
		stout.println("'q' - close connection, and exit client.");
		stout.println("'list' - lists all available commands.");
		stout.println("'start' - start playing the game. Default difficulty: 5");
		stout.println("'diff arg' - set difficulty; arg = 1 --> 9");
		
		stout.println("> ------------------------------ <\n");
		
	}
	
	/**
	 * Prints out message of invalid command.
	 */
	private void printInvalid()
	{
		stout.println("Invalid command. For full list of accepted commands, enter 'list'");
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
	private void establishConnection()
	{
		try
		{
			stout.print("Establishing connection to server... ");
		
			TP03Decoder decoder = new TP03Decoder();
			conn = decoder.makeConnection(new URI(URIString), true, new TP03Visitor(false));
			DefaultConnectionListener<TP03Visitor> listener = new DefaultConnectionListener<TP03Visitor>();
			conn.addConnectionListener(listener);
		
			stout.println("connection established.");
		}
		catch (Exception e)
		{
			stout.println("Error connecting to server.");
			stout.println(e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	
	/**
	 * Sets the difficulty of the AI opponent.
	 * @param diff between 1 --> 9.
	 * @param verbose display or not.
	 */
	private void setDifficulty(int diff, boolean verbose)
	{
		this.difficulty = diff;
		if (verbose)
			stout.println("Difficulty set to " + this.difficulty + "/9");
	}
	
	/**
	 * Start playing game.
	 */
	private void startPlay()
	{
		stout.println("Starting to play game... ");
		//recieveFramesSynch();
	}
	
	/*
	private void recieveFramesSynch()
	{
		try
		{
			stout.print("Recieving all frames synchronously... ");
			conn.receiveAllFrames(this);
			stout.println("done.");
		}
		catch (Exception e)
		{
			stout.println("Failed to synchronously fetch frames");
			stout.println(e.getMessage());
			e.printStackTrace();
		}
	}
	*/
	
	
	
	
	/**
	 * Closing connection, and exiting client.
	 * @param message Exit message.
	 */
	private void exit(String message)
	{
		try
		{
			stout.println("Exit: " + message);
			stout.print("Closing connection... ");
			conn.close();
			stout.println("done.");
			stout.println("Farewell, farewell; goodbye is such sweet sorrow.");
			System.exit(0);
		}
		catch (IOException e)
		{
			stout.println("Error closing the connection.");
			stout.println(e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
}
