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
	final int NORMAL_EXIT = 0;
	final int ABNORMAL_EXIT = -1;
	PrintStream stout = System.out; 
	Scanner stin = new Scanner(System.in);
	
	//client-related
	private URI serverURI;
	private SequentialConnection<TP03Visitor> conn;
	private PipelinedConnection<TP03Visitor> PipeConn;
	

	final String QUIT = "q";

	private boolean verboseDebugMode = false; // False by default.
	
	//game-related
	private int difficulty;
	
	/**
	 * Runner method for the client.
	 * 
	 * @param args Optional: '-a serverURI' to autologin to server. The serverURI must include user info, 
	 * e.g.: "tp://guest:guest@thousandparsec.net/tp". If none provided, user will be manually prompted for 
	 * user info and server address, without autologin.
	 * 
	 */
	void runClient(String[] args)
	{
		
		stout.println("GenCon (Genetic Conquest): An AI Client for Thousand Parsec : RFTS ruleset.\n");
		stout.println("Follow the instructions. To quit, enter '" + QUIT + "' at any time when prompted for input.");
		
		// setting verbose duebug mode on/off
		// client.setVerboseDebug();
		
		String URIstr = "";
		
		try
		{
			for (int i = 0; i < args.length; i++)
			{
				if (args[i].equals("-a"))
				{
					URIstr = args[i + 1];
				}
				if (args[i].equals("-v"))
				{
					verboseDebugMode = true;
				}
			}
		}
		catch (IndexOutOfBoundsException e)
		{
			exit("Input error in argument. The optional arguments syntax is: '-a URIstring -v'\n" +
					"'-a URIstring' autologins as user, and '-v' activates verbose debug mode. Try again.", ABNORMAL_EXIT, null);
		}
		
		if (URIstr.equals(""))
			initNoAutologin();
		else
			initAutologin(URIstr);
		
	}
	
	/**
	 * New instance of the client.
	 *
	 */
	Client(){}
	

	/**
	 * Initializes client. URI string provided by standard input.
	 * No autologin.
	 */
	void initNoAutologin()
	{
		// setting the URI:
		boolean uriOk = false;
		while (!uriOk)
		{
	 		stout.print("Enter the URI of the server (without user info; autologin disabled) : ");
			String URIString = stin.next();
			
			quitIfEncounterExitString(URIString);
			
			uriOk = setURI(URIString);
			if (!uriOk)
				stout.println("Try again.");
		}
		
		// establish a connection with the server, no autologin.
		establishPipelinedConnection(false);

		//login as existing user, or create new user and then login
		loginOrCreateUser();
		
		////// FINISHED INITIALIZING. CLIENT IS NOW CONNECTED AND LOGGED IN AS A USER ////////
		
		//run the main command interface of the client
		runCommands();
	}
	
	/**
	 * Initializes client with previously specified URI string. Autologin enabled.
	 * @param URI {@link URI} string (with user info).
	 */
	void initAutologin(String URIstring)
	{
		//setting the URI string. 
		boolean success = setURI(URIstring);
		if (!success)
		{
			stout.println("Attempt manual entry of URI.");
			initNoAutologin();
		}

		// establish a connection with the server, with autologin
		establishPipelinedConnection(true);
		
		//run the main command interface of the client
		runCommands();
	}
	
	/*
	 * Making the server URI from a string.
	 * Retrurns true if successful, false otherwise.
	 */
	private boolean setURI(String URIString)
	{
		boolean success = true;
		URI trySetURI;
		try
		{
			trySetURI = new URI(URIString);
			this.serverURI = trySetURI;
		}
		catch (URISyntaxException e)
		{
			stout.println("Error: URI syntax incorrect.");
			PrintTraceIfDebug(e);
			success = false;
		}
		
		return success;
	}
	
	/*	CURRENTLY NOT IN USE
	 *	Sets verbose debug mode on/off (from standard input). 
	 *
	void setVerboseDebug()
	{
		boolean repeat = false;
		do
		{
			repeat = false;
			stout.print("Verbose debug mode? (y / n) : ");
			String input = stin.next();
			
			quitIfEncounterExitString(input);
			
			if (input.equals("y"))
				this.verboseDebugMode = true;
			else if (input.equals("n"))
				this.verboseDebugMode = false;
			else
			{
				stout.println("Invalid input. Try again.");
				repeat = true;
			}
		} while (repeat == true);
	}
	*/
	
	/*
	 * Prints exception stack trace, if verbose debug mode is on.
	 */
	private void PrintTraceIfDebug(Exception e)
	{
		if (verboseDebugMode)
		{
			e.printStackTrace();
		}
	}

	/*
	 * Establishes a pipelined connection with the server. Sets one pipeline (SequentialConnection) as the main connection of the client.
	 * Uses TP03 protocol classes.
	 * Autologin on/off, depends on the user
	 */
	private void establishPipelinedConnection(boolean autologin)
	{
		try
		{
			stout.print("Establishing connection to server... ");
		
			TP03Decoder decoder = new TP03Decoder();
			Connection<TP03Visitor> basicCon = decoder.makeConnection(serverURI, autologin, new TP03Visitor(false));
			
			DefaultConnectionListener<TP03Visitor> listener = new DefaultConnectionListener<TP03Visitor>();
			basicCon.addConnectionListener(listener);
			
			PipelinedConnection<TP03Visitor> pConn = new PipelinedConnection<TP03Visitor>(basicCon);
			
			this.PipeConn = pConn;
			this.conn = pConn.createPipeline();
			
			
			stout.println("connection established.");
		}
		catch (Exception e)
		{
			exit("Error connecting to server.", ABNORMAL_EXIT, e);
		}
	}

	/*
	 * Logs in as user, or else creates new account and logs in as that user.
	 * User info gathered from standard input.
	 * 
	 * ~STILL IN PROTOTYPICAL FORM
	 * ~ASSUMPTION: 'create new user' does not automatically log you in as that user; rather, the client needs to manually log in afterwards.
	 */
	private void loginOrCreateUser()
	{
		boolean retry = false;
		do 
		{
			stout.print("Create new account or login as existing user? (new / login) : ");
			String choose = stin.next();
			if (choose.equals("login"))
			{
				stout.println("Logging in as:");
				String[] user = enterUserDetails();
				String username = user[0];
				String password = user[1];
				retry = login(username , password);
			}
			else if (choose.equals("new"))
			{
				String[] user = enterUserDetails();
				String username = user[0];
				String password = user[1];
				retry = createNewAccount(username, password);
				retry = login(username, password);
			}
			else
			{
				stout.println("Invalid input. Try again.");
				retry = true;
			}
		} while (retry == true);
	}
	
	/*
	 * Simple method that queries for user details.
	 * Returns String[], where the first index holds username, and the second holds password.
	 * TO DO: Make password entry invisible on screen!!!
	 */
	private String[] enterUserDetails()
	{
		String[] userDetails = new String[2];
		
		stout.print("Enter username: ");
		String usrname = stin.next();
		quitIfEncounterExitString(usrname);
		userDetails[0] = usrname;
		
		stout.print("Enter password: ");
		String pwd = stin.next();
		quitIfEncounterExitString(pwd);
		userDetails[1] = pwd;
		
		return userDetails;
	}
	
	
	/*
	 * Runs the main command interface of the client. Ideally should run on a separate thread (TO DO!)
	 */
	private void runCommands()
	{	
		//set to default:
		setDifficulty(5, false);
		
		//execute command-line orders, until encountering control character 'q'
		stout.println("Enter command. 'list' shows all available commands.");	
		
		while(true)
		{
			stout.print("Enter command > ");
			String command = stin.nextLine();
			
			quitIfEncounterExitString(command);
			 
			if (command.equals("list"))
				printCommands();
			
			else if (command.equals("start"))
				startPlay();
			
			else if (command.length() == 6 && 
					command.substring(0, 4).equals("diff") && 
					command.substring(5, 6).matches("[1-9]"))
			{
				try
				{
					stout.println("Booya!");
					setDifficulty(new Integer(command.substring(5,6)).intValue(), true);
				}
				catch (Exception e)
				{
					printInvalid();
				}
			}
			else 
				printInvalid();
			
		}
	}
	
	/*
	 * Prints out list of accepted commands.
	 */
	private void printCommands()
	{
		stout.println("\n> The list of available commands (Case sensitive) <");
		stout.println("'" + QUIT + "' - exits client.");
		stout.println("'list' - lists all available commands.");
		stout.println("'start' - start playing the game. Default difficulty: 5");
		stout.println("'diff $' - set difficulty of AI; $ = 1 --> 9");
		
		stout.println();
//	stout.println("> ------------------------------ <\n");
		
	}
	
	/*
	 * Prints out message of invalid command.
	 */
	private void printInvalid()
	{
		stout.println("Invalid command. For full list of accepted commands, enter 'list'");
	}
	


	/*
	 * Sets the difficulty of the AI opponent.
	 * @param diff between 1 --> 9.
	 * @param verbose display on screen or not.
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
			PrintTraceIfDebug(e);
		}
	}
	*/
	
	
	
	
	/*
	 * Closing connection, and exiting client.
	 * @param message Exit message.
	 */
	private void exit(String message, int exitType, Exception exc)
	{
		
		try
		{
			stout.println("\nClosing GenCon: " + message);
			if (PipeConn != null)
			{
				stout.print("Closing connection... ");
				PipeConn.close();
				stout.println("done.");
			}
			//getting fancy
			//stout.println("Farewell, farewell; goodbye is such sweet sorrow.");
			
			stout.println("Successful exit.");
			
			//if there is an exception, print trace if verbose debug mode is on.
			if (exc != null)
				PrintTraceIfDebug(exc);
			
			System.exit(exitType);
		}
		catch (Exception e)
		{
			stout.println("Error closing the connection. Exiting application.");
			PrintTraceIfDebug(e);
			System.exit(ABNORMAL_EXIT);
		}
	}
	
	/*
	 * Exit client if encounter the QUIT string in standard input.
	 */
	private void quitIfEncounterExitString(String str)
	{
		if (str.equals(QUIT))
			exit("Manual exit.", NORMAL_EXIT, null);
	}
	
}