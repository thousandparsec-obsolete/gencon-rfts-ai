package gencon;

import java.io.*;
import java.net.*;
import java.util.*;

import gencon.utils.*;
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
	public static final int NORMAL_EXIT = 0;
	public static final int ABNORMAL_EXIT = -1;
	private static final PrintStream stout = System.out; 
	public static final String QUIT = "q";
	
	private final ScannerListener stin;
	private boolean verboseDebugMode = true; // True by default.
	
	//connection-related
	private URI serverURI;
	private PipelinedConnection<TP03Visitor> PipeConn;
	private final LoggerConnectionListener<TP03Visitor> eventLogger;
	private final TP03Visitor visitor;

	//game-related
	private int difficulty = 5;
	
	
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
	
	/**
	 * The default constructor.
	 *
	 */
	Client()
	{
		//starting up the input listener
		stin = new ScannerListener(new Scanner(System.in), this);
		eventLogger = new LoggerConnectionListener<TP03Visitor>();
		visitor = new TP03Visitor(false);
	}
	
	
	
	public void runClient(String[] args)
	{
		
		stout.println("GenCon (Genetic Conquest): An AI Client for Thousand Parsec : RFTS ruleset.");
		stout.println("To quit, enter '" + QUIT + "' at any time, then press RETURN.\n");
		
		String URIstr = "";
		
		if (args.length == 0) {/* NORMAL OPERATION OF CLIENT */}
		
		//configuring options for autorun.
		else if ((args.length == 2 || args.length == 3) && args[0].equals("-a"))
		{
			URIstr = args[1];
			try
			{
				if (args.length == 3)
				{
					difficulty = new Integer(args[2]).intValue();
					if (!(difficulty > 0 && difficulty < 10)) //difficulty between 1-9
						throw new Exception();
				}
				stout.println("Difficulty set to " + difficulty);
					
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

/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * 
 *	INITIALIZATION: NORMAL OPERATION OR AUTORUN
 * 
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
	
	/*
	 * NORMAL OPERATION.
	 * Initializes client. URI string provided by standard input.
	 * No autologin.
	 */
	private void initNoAutorun()
	{
		stout.println("Follow the instructions...");
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
	 * AUTORUN.
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
	

	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * 
	 *	METHODS FOR USER INPUT IN NORMAL OPERATION OF CLIENT
	 * 
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 */
	
	private void setVerboseDebug()
	{
		boolean ok = true;
		do {
			stout.print("Verbose debug mode? (y / n) : ");
			String in = stin.next();
			if (in.equals("y"))
			{
				verboseDebugMode = true;
				stout.println("Verbose debug mode set to : " + verboseDebugMode);
				return;
			}
			else if (in.equals("n"))
			{
				verboseDebugMode = false;
				stout.println("Verbose debug mode set to : " + verboseDebugMode);
				return;
			}
			else
			{
				stout.println("Please enter 'y' or 'n'.");
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
			
			//quitIfEncounterExitString(URIString);
			
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
	 * Sets the difficulty of the AI opponent.
	 */
	private void setDifficulty()
	{
		boolean ok = true;
		do {
			
			
			
			
			
			stout.print("Set difficulty of AI player (1 to 9) : ");
			int num = -1; 
			try
			{
				num = new Integer(stin.next()).intValue();
				if (num > 0 && num < 10)
					ok = true;
			}
			catch (Exception e)
			{
				stout.println("Invalid input. Enter a number between 1 to 9. Try again.");
				ok = false;
			}
			if (ok)
			{
				difficulty = num;
				stout.println("Difficulty set to : " + difficulty);
			}
		} while (!ok);
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
			
			Connection<TP03Visitor> basicCon = decoder.makeConnection(serverURI, autologin, visitor);
			
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
	

	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * 
	 *	CONNECTION METHODS USED IN NORMAL OPERATION OF CLIENT.
	 * 
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 */

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
		//	quitIfEncounterExitString(choose);
			
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
	//	quitIfEncounterExitString(usrname);
		userDetails[0] = usrname;
		
		stout.print("Enter password: ");
		String pwd = stin.next();
	//	quitIfEncounterExitString(pwd);
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
	
	

	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * 
	 *	STARTING TO PLAY!
	 * 
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 */
	
	/*
	 * Start playing game.
	 */
	private void startPlay()
	{
		stout.println("Starting to play game... ");
		//INVOKING A TEST METHOD
		recieveFramesAsynch();
		
		//testing Scanner Listener:
		/*
		stout.println("Waiting for exit string... ");
		while (true);
		*/
		try
		{
			Thread.sleep(10000); //sleep for a while
		}
		catch (InterruptedException e)
		{
			exit("Sleep interrupted", ABNORMAL_EXIT, e);
		}
		
		
		
		//exiting
		exit("Finished playing.", NORMAL_EXIT, null);
	}
	

	/*
	 * REALLY, A TEST METHOD
	 */
	private void recieveFramesAsynch()
	{
		try
		{
			stout.print("Recieving all frames asynchronously... ");
			SequentialConnection<TP03Visitor> c= PipeConn.createPipeline();
			SequentialConnection<TP03Visitor> d= PipeConn.createPipeline();
			c.sendFrame(new GetTimeRemaining(), visitor);
			eventLogger.dumpLogStd();
			d.sendFrame(new GetObjectIDs(), visitor);
			c.sendFrame(new GetTimeRemaining(), visitor);
			eventLogger.dumpLogStd();
			c.close();
			d.close();
			
			stout.println("done.");
		}
		catch (Exception e)
		{
			stout.println("Failed to synchronously fetch frames");
			PrintTraceIfDebug(e);
		}
	}
	
	

	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * 
	 *	MAINTANANCE METHODS
	 * 
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 */
	
	
	/*
	 * Closing connection, and exiting client.
	 * @param message Exit message.
	 */
	private synchronized void exit(String message, int exitType, Exception exc)
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
			
			//if there is an exception, print trace if verbose debug mode is on.
			if (exc != null)
				PrintTraceIfDebug(exc);
			
			
			
			// interrupting the ScannerListener.
			stin.close();
			
			stout.println("\nClean exit.");
			//closing standard out.
			stout.close();
			
			System.exit(exitType);
		}
		catch (Exception e)
		{
			stout.println("Error on exit. Quitting application.");
			PrintTraceIfDebug(e);
			System.exit(ABNORMAL_EXIT);
		}
	}
	
	/*
	 * Prints exception stack trace, if verbose debug mode is on.
	 * Avoids the mess of the usual exception.printStackTrace(), 
	 * where it prints in System.err, in parallel with System.out.
	 * This way, info will remain chronologically consistent.
	 * 
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
			
			stout.println("______________________________________");
		}
	}
	
	/**
	 * Used by the {@link ScannerListener} class, to notify the Client that
	 * the exit string has been encountered. 
	 */
	public void exitOnEncounteringExitString()
	{
		exit("Exit string '" + QUIT + "' encountered. Exiting Client...", NORMAL_EXIT, null);
	}
		
}