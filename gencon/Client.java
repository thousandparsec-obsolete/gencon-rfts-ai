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
		
		Client client = new Client();
		
		if (args.length > 0 && args[0].equals("-c"))
			client.init(args[1]);
		else if (args.length == 0)
			client.init();
		else
		{
			System.out.print("Input error. Try again.");
			System.exit(-1);
		}
	}
	
	/**
	 * New instance of the client.
	 *
	 */
	Client(){}
	

	/**
	 * initializes client. URI string given by standard input.
	 *
	 */
	void init()
	{
 		stout.print("Enter the address of the server : ");
		String address = stin.next();
		
		stout.println("Create new account or use existing? (enter: 'n' or 'e'");
		
		boolean retry = false;
		do
		{
			String login = stin.next();
			if (login.equals("e"))
				setURIfromStdinput(address);
			else if (login.equals("n"))
			{
				//the first string in user[] is username, the second is password.
				String[] user = createNewAccount(address);
				setURI(user[0], user[1], address); 
			}
			else
			{
				stout.println("Invalid input. Try again.");
				retry = true;
			}
		} while (retry == true);
		
		// ~~~ NO NEED TO ESTABLISH CONNECTION, AS IT HAS BEEN DONE IN createNewAccountAndLogin(server)
		//first establish a connection with the server
		establishConnection();

		//run client
		run();
	}
	
	/**
	 * Initializes client with specified URI string.
	 * @param URI the string that specifies the address of the server, the username, and the password
	 */
	void init(String URI)
	{
		this.URIString = URI;
		
		//first establish a connection with the server
		establishConnection();
		
		//run client
		run();
	}
	
	private void setURIfromStdinput(String address)
	{
		stout.print("Enter username : ");
		String usrname = stin.next();
		stout.print("Enter password : ");
		String pwd = stin.next();
		
		setURI(usrname, pwd, address);
	}
	
	
	//this is VERY erroneous; TOP priority fix
	private void setURI(String usrname, String pwd, String address)
	{
		this.URIString = "tp://" + pwd + ":" + usrname + "@" + address;
	}
	
	/*
	 * 
	 * @return String[] : index 0 is username, index 1 is password.
	 */
	private String[] createNewAccount(String server)
	{
		return null;
	}
	
	/*
	 * Runs the client.
	 */
	private void run()
	{	
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
	
	/*
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
	
	/*
	 * Prints out message of invalid command.
	 */
	private void printInvalid()
	{
		stout.println("Invalid command. For full list of accepted commands, enter 'list'");
	}
	
	
	
/*
 * Establishes a connection with the server.
 * 
 */
	private void establishConnection()
	{
		try
		{
			stout.print("Establishing connection to server... ");
		
			TP03Decoder decoder = new TP03Decoder();
			conn = decoder.makeConnection(new URI(this.URIString), true, new TP03Visitor(false));
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
	
	
	/*
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
	
	/*
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
	
	
	
	
	/*
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
			stout.println("Error closing the connection. Exiting application.");
			stout.println(e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
}
