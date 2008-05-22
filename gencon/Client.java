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
	 * Run this method to start the client.
	 * 
	 * @param args Optional arguments: '-a serverURI' and '-v'. 
	 * To autologin to server as an existing user, type in '-a serverURI'. 
	 * The serverURI must include user info, e.g.: "tp://guest:guest@thousandparsec.net/tp". If none provided, 
	 * user will be manually prompted for user info and server address, without autologin.
	 * To turn on verbose debug mode, type in '-v'.
	 * 
	 */
	public void runClient(String[] args)
	{
		
		stout.println("GenCon (Genetic Conquest): An AI Client for Thousand Parsec : RFTS ruleset.\n");
		stout.println("Follow the instructions. To quit, enter '" + QUIT + "' at any time when prompted for input.");
		
		String URIstr = "";
		
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-a") && i < args.length - 1)
			{
				URIstr = args[i + 1];
			}
			else if (args[i].equals("-v"))
			{
				verboseDebugMode = true;
				stout.println("Verbose debug mode : on");
			}
			else if (i - 1 >= 0 && args[i - 1].equals("-a"))
			{ /* legal case; the URI string */ }
			//case: illegal argument
			else
			{
				exit("Input error in arguments.", ABNORMAL_EXIT,
						new IllegalArgumentException("Illegal Arguments. \nThe optional arguments syntax are: '-a URIstring', and '-v'.\n" +
						"'-a URIstring' autologins as user, and '-v' activates verbose debug mode. \nTry again."));
			}
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
		
			//////	 FINISHED INITIALIZING. CLIENT IS NOW CONNECTED AND LOGGED IN AS A USER ////////
		
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
		
			//////	 FINISHED INITIALIZING. CLIENT IS NOW CONNECTED AND LOGGED IN AS A USER ////////
		
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
	
	/*
	 * Prints exception stack trace, if verbose debug mode is on.
	 * Avoids the mess of the usual exception.printStackTrace(), 
	 * where it prints in parallel with other things.
	 */
	private void PrintTraceIfDebug(Exception e)
	{
		if (verboseDebugMode)
		{
			stout.println("______________________________________");
			stout.println("DEBUG:");
			stout.println("Exception: " + e.getClass().getName());
			stout.println("Cause: " + e.getMessage());
			stout.println("Stack trace:");
			StackTraceElement[] stackTrace = e.getStackTrace();
			for (StackTraceElement ste : stackTrace)
				stout.println(ste.toString());
			
			stout.println("______________________________________\n");
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
				
				if (!login(username , password))
				{
					stout.println("Failed to login. Try again.");
					retry = true;
				}
			}
			else if (choose.equals("new"))
			{
				String[] user = enterUserDetails();
				String username = user[0];
				String password = user[1];
				if (createNewAccount(username, password))
				{
					if (!login(username, password))
					{
						stout.println("Unexpected failure to login after creating account. Try logging as the new user.");
						retry = true;
					}
					
				}
				else
				{
					stout.println("Failed to create account. Try again.");
					retry = true;
				}
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
			
			//if there is an exception, print trace if verbose debug mode is on.
			if (exc != null)
				PrintTraceIfDebug(exc);
			
			stout.println("\nSuccessful exit.");
			
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