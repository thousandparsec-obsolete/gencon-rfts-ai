package gencon;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.Future;

import net.thousandparsec.netlib.*;
import net.thousandparsec.netlib.tp03.*;


/**
 * This is the basic client for GenCon. It complies with TP03 Protocol.
 * Its sole functionality is to connect, log in, then send frames specified from outside,
 * and pass the received frames outside as well.
 * 
 * @author Victor Ivri
 *
 */
public class Client
{
	//maintanence
	private static final int NORMAL_EXIT = 0;
	private static final int ABNORMAL_EXIT = -1;
	private final PrintStream stout = System.out; 
	private final Scanner stin = new Scanner(System.in);
	public static final String QUIT = "q";
	private boolean verboseDebugMode = false; // False by default.
	
	//connection-related
	private URI serverURI;
	private PipelinedConnection<TP03Visitor> PipeConn;
	private LoggerConnectionListener<TP03Visitor> eventLogger;

	//game-related
	private int difficulty;
	
	
	/**
	 * Run this method to start the client.
	 * 
	 * @param args Optional argument: '-a serverURI $' 
	 * To autorun client, supply argument '-a', followed by the 'serverURI' and by game difficulty '$', 
	 * which should be replaced by any number 1 to 9. If no game difficulty provided, default is 5.
	 * The serverURI must include user info for autologin, e.g.: "tp://guest:guest@thousandparsec.net/tp".
	 * In this case, verbose debug mode will be automatically on.
	 * 
	 * If no argument provided, client will start in 'normal' mode; that is, it will rely on standard user input. 
	 */
	public void runClient(String[] args)
	{
		
		stout.println("GenCon (Genetic Conquest): An AI Client for Thousand Parsec : RFTS ruleset.\n");
		
		String URIstr = "";
		
		if (args.length == 0) {/* NORMAL OPERATION OF CLIENT */}
		
		//determining autorun.
		else if ((args.length == 2 || args.length == 3) && args[0].equals("-a"))
		{
			verboseDebugMode = true;
			URIstr = args[1];
			try
			{
				if (args.length == 3)
				{
					difficulty = new Integer(args[2]).intValue();
					if (difficulty > 0 && difficulty < 10) //difficulty between 1-9
						stout.println("Difficulty set to " + difficulty);
					else
						throw new Exception();
				}
			}
			catch (Exception e)
			{
				exit("Input error in arguments.", ABNORMAL_EXIT,
						new IllegalArgumentException("Illegal Arguments. See documentation for proper syntax. Try again."));
			}
		}
		
		else
			exit("Input error in arguments.", ABNORMAL_EXIT, 
				new IllegalArgumentException("Illegal Arguments. See documentation for proper syntax. Try again."));
		
		
		if (URIstr.equals(""))
			initNoAutorun();
		else
			initAutorun(URIstr);
		
	}
	
	/**
	 * New instance of the client.
	 *
	 */
	Client(){}
	

	/*
	 * Initializes client. URI string provided by standard input.
	 * No autologin.
	 */
	private void initNoAutorun()
	{
		stout.println("Follow the instructions. To quit, enter '" + QUIT + "' at any time when prompted for input.");
		
		//set verbose debug mode on/off
		setVerboseDebug();
		
		//set URI
		manualSetURI();
		
		//set AI difficulty
		setDifficulty();
		
		// establish a connection with the server, no autologin.
		establishPipelinedConnection(false);

		//login as existing user, or create new user and then login
		loginOrCreateUser();
		
		//let the games begin!
		startPlay();
	}
	
	/*
	 * Initializes client with previously specified URI string. Autologin enabled.
	 * @param URI {@link URI} string (with user info).
	 */
	private void initAutorun(String URIstring)
	{
		stout.println("Autorun mode. Initializing...");
		
		if (setURI(URIstring)) //if URI is valid, proceed with normal operation
		{
			// establish a connection with the server. autologins as player.
			establishPipelinedConnection(true);
		
			//let the games begin!
			startPlay();
		}
		 //Quit client otherwise. 
		 //The rationale is that autorun will most likely be part of a test suite, 
		 //and there will be no room for user corrections.
		else 
			exit("Invalid URI. Exiting autorun.", ABNORMAL_EXIT, null);
	}
	
	
	private void setVerboseDebug()
	{
		boolean ok = true;
		do {
			stout.println("Verbose debug mode: (y / n) ");
			String in = stin.next();
			if (in.equals("y"))
				verboseDebugMode = true;
			else if (in.equals("n"))
				verboseDebugMode = false;
			else
			{
				stout.println("Please enter 'y' or 'n'.");
				ok = false;
			}
			
			if (ok)
				stout.println("Verbose debug mode set to : " + in);
		} while (!ok);
	}
	
	/*
	 * Sets the difficulty of the AI opponent.
	 */
	private void setDifficulty()
	{
		boolean ok = true;
		do {
			stout.println("Set difficulty of AI player: (1 to 9)");
			try
			{
				difficulty = stin.nextInt();
				stout.println("Difficulty set to : " + difficulty);
			}
			catch (Exception e)
			{
				stout.println("Invalid input. Enter a number between 1 to 9. Try again.");
				ok = false;
			}
			
		} while (!ok);
	}
	
	
	/* 
	 * set the URI by standard user input.
	 */
	private void manualSetURI()
	{
		// setting the URI:
		boolean uriOk = false;
		do {
	 		stout.print("Enter the URI of the server (without user info; autologin disabled) : ");
			String URIString = stin.next();
			
			quitIfEncounterExitString(URIString);
			
			uriOk = setURI(URIString);
		} while (!uriOk);
	}
	
	/*
	 * Making the server URI from a string.
	 * Retrurns true if successful, false otherwise.
	 */
	private boolean setURI(String URIString)
	{
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
			return false;
		}
		return true;
	}
	
	/*
	 * Establishes a pipelined connection with the server. Sets one pipeline (SequentialConnection) as the main connection of the client.
	 * Uses TP03 protocol classes.
	 * Autologin on/off, depends on the user
	 */
	private void establishPipelinedConnection(boolean autologin)
	{
		TP03Decoder decoder = new TP03Decoder();
		try
		{
			stout.print("Establishing connection to server... ");
			
			Connection<TP03Visitor> basicCon = decoder.makeConnection(serverURI, autologin, new TP03Visitor(false));
			
			basicCon.addConnectionListener(eventLogger);
			
			PipelinedConnection<TP03Visitor> pConn = new PipelinedConnection<TP03Visitor>(basicCon);
			this.PipeConn = pConn;
			
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
	 * ~ASSUMPTION: 'create new user' does not automatically log you in as that user; 
	 * rather, the client needs to manually log in afterwards.
	 * ~Appears to be the case
	 * 
	 */
	private void loginOrCreateUser()
	{
		boolean retry = false;
		do 
		{
			stout.print("Create new account or login as existing user? (new / login) : ");
			String choose = stin.next();
			quitIfEncounterExitString(choose);
			
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
						stout.println("Unexpected failure to login after creating account. Try logging as the new user manually.");
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
		} while (retry);
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
	
	//IN VERY PROTOTYPICAL FORM!!!
	//WILL EXTRACT INFO FROM LOGGER, NOT FROM PARSING STRINGS!!!
	private boolean login(String username, String password)
	{
		Login loginFrame = new Login();
		loginFrame.setUsername(username);
		loginFrame.setPassword(password);
		
		//the expected response
		Class okay = Class.forName("net.thousandparsec.netlib.tp03.Okay");
		
		try
		{
			//synchronously sends, and waits for okay
			conn.sendFrame(loginFrame, okay);
		}
		catch (TPException tpe)
		{
			if (tpe.getMessage().startsWith("Response"))
			{
				stout.println("Unexpected failure: Sequence numbers did not match. Try again.");
				PrintTraceIfDebug(tpe);
				loginOrCreateUser();
			}
			else if (tpe.getMessage().startsWith("Unexpected"))
			{
				//checking whether it's redirect or fail:
				StringTokenizer st = new StringTokenizer(tpe.getMessage());
				
				//iterate tokens in the error message, until get to the one that says "redirect" or "fail" in the frame type:
				for (int i = 0; i < 4; i++)
					st.nextToken();
				
				String theFrame = st.nextToken();
				
				stout.println("Failed to login. Possible cause: . Try again.");
				PrintTraceIfDebug(tpe);
				loginOrCreateUser();PrintTraceIfDebug(tpe);
			}
			else
			{
				PrintTraceIfDebug(tpe);
			}
		}
		catch (EOFException eofe)
		{
			stout.println("Unexpected failure: No frame received from server. Try again.");
			PrintTraceIfDebug(eofe);
			loginOrCreateUser();
		}
		catch (IOException ioe)
		{
			stout.println("Unexpected failure. Try again.");
			PrintTraceIfDebug(ioe);
			loginOrCreateUser();
		}
		
		
		
		
		
	}
	
	
	
	
	
	/*
	 * Start playing game.
	 */
	private void startPlay()
	{
		stout.println("Starting to play game... ");
		//INVOKING A TEST METHOD
		recieveFramesAsynch();
	}
	

	/*
	 * REALLY, A TEST METHOD
	 */
	private void recieveFramesAsynch()
	{
		try
		{
			stout.print("Recieving all frames asynchronously... ");
			conn.getConnection().receiveAllFramesAsync(new TP03Visitor());
			stout.println("done.");
		}
		catch (Exception e)
		{
			stout.println("Failed to synchronously fetch frames");
			PrintTraceIfDebug(e);
		}
	}
	
	
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
			
			stout.println("\nClean exit.");
			
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
			stout.println("Exception in : " + e.getClass().getName());
			stout.println("Cause : " + e.getMessage());
			stout.println("Stack trace:");
			StackTraceElement[] stackTrace = e.getStackTrace();
			for (StackTraceElement ste : stackTrace)
				stout.println(ste.toString());
			
			stout.println("______________________________________\n");
		}
	}
	
}