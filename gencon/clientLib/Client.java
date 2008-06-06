package gencon.clientLib;

import java.io.*;
import java.net.*;
import java.util.*;

import gencon.utils.*;
import net.thousandparsec.netlib.*;
import net.thousandparsec.util.*;
import net.thousandparsec.netlib.tp03.*;
import net.thousandparsec.netlib.tp03.Object;


/**
 * This is the basic client for GenCon. It complies with TP03 Protocol.
 * Its sole functionality is to connect, log in, then send frames specified from outside,
 * and pass the received frames outside as well.
 * 
 * @author Victor Ivri
 *
 */
public class Client <V extends Visitor>
{
	//maintanence
	public static final int NORMAL_EXIT = 0;
	public static final int ABNORMAL_EXIT = -1;
	private static final PrintStream stout = System.out; 
	public static final String QUIT = "q";
	
	private final ScannerListener stin;
	private boolean verboseDebugMode = true; // True by default.
	private boolean autorun;
	
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
	public Client()
	{
		//starting up the input listener
		stin = new ScannerListener(new Scanner(System.in), this);
		eventLogger = new LoggerConnectionListener<TP03Visitor>();
		visitor = new GCTP03Visitor();
	}
	
	
	
	public void runClient(String[] args)
	{
		
		stout.println("GenCon (Genetic Conquest): An AI Client for Thousand Parsec : RFTS ruleset.");
		stout.println("To quit at any time, enter '" + QUIT + "', then press RETURN.\n");
		
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
		autorun = false;
		
		stout.println("Follow the instructions...");
		//set verbose debug mode on/off
		setVerboseDebug();
		
		//set URI
		manualSetURI();
		
		//set AI difficulty
		setDifficulty();
		
		// establish a connection with the server, no autologin.
		establishPipelinedConnection();

		//login as existing user, or create new user and then login
		loginOrCreateUser();
		
		//let the games begin!
		startPlay();
		
		exit("Finished playing.", ABNORMAL_EXIT, null);
	}
	
	/*
	 * AUTORUN.
	 * Initializes client with previously specified URI string. Autologin enabled.
	 * @param URI {@link URI} string (with user info).
	 */
	private void initAutorun(String URIstring)
	{
		stout.println("Autorun mode. Initializing...");
		
		autorun = true;
		
		if (setURI(URIstring)) //if URI is valid, proceed with normal operation
		{
			// establish a connection with the server. autologins as player.
			establishPipelinedConnection();
		
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
			Utils.PrintTraceIfDebug(e, verboseDebugMode);
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
	 * Establishes a pipelined connection with the server.
	 * Uses TP03 protocol classes.
	 * Autologin on/off, depends on the user
	 */
	private void establishPipelinedConnection()
	{
		TP03Decoder decoder = new TP03Decoder();
		try
		{
			stout.print("Establishing connection to server... ");
			
			Connection<TP03Visitor> basicCon = decoder.makeConnection(serverURI, autorun, visitor);
			
			basicCon.addConnectionListener(eventLogger);
			
			PipelinedConnection<TP03Visitor> pConn = new PipelinedConnection<TP03Visitor>(basicCon);
			this.PipeConn = pConn;
			
			stout.println("connection established.");
			if (autorun)
				stout.println("Logged in successfully.");
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
				String[] user = enterUserDetails();
				String username = user[0];
				String password = user[1];
				
				if (!login(username , password))
					retry = true;
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
	private boolean login(String username, String password)
	{
		Connect connectFrame = new Connect();
		connectFrame.setString("gencon-testing");
		
		Login loginFrame = new Login();
		loginFrame.setUsername(username);
		loginFrame.setPassword(password);
		
		Class okay = Utils.getClass("net.thousandparsec.netlib.tp03.Okay");
		if (okay == null)
			exit("Wrong classpath for Okay frame", ABNORMAL_EXIT, null);
		
		try
		{
			//will be supplanted by the ThreadedPipelineManager methods... sometime... in the future...
			SequentialConnection<TP03Visitor> conn = PipeConn.createPipeline();
			stout.print("Logging in...");
			conn.sendFrame(connectFrame, okay);
			stout.println("...");
			conn.sendFrame(loginFrame, okay);
			conn.close();
			stout.println("Login successful");
		}
		catch (TPException tpe)
		{
			stout.println("Unexpected failure: Protocol failure. Failed to login. Try again.");
			Utils.PrintTraceIfDebug(tpe, verboseDebugMode);
			return false;
		}
		catch (EOFException eofe)
		{
			stout.println("Unexpected failure: End of stream reached. Failed to login. Try again.");
			Utils.PrintTraceIfDebug(eofe, verboseDebugMode);
			exit("No more frames sent from server.", NORMAL_EXIT, eofe);
		}
		catch (IOException ioe)
		{
			stout.println("Unexpected failure. Failed to login. Try again.");
			Utils.PrintTraceIfDebug(ioe, verboseDebugMode);
			return false;
		}
		
		return true;
	}
	
	private boolean createNewAccount(String username, String password)
	{
		//TO BE DONE!!
		
		return false;
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
		Vector<Pair<Integer, Object>> objects = recieveObjects();
		
		stout.println("Printing objects:");
		for (Pair<Integer, Object> pair : objects)
		{
			if (pair != null)
				if (pair.right != null)
					stout.println("Object: " + pair.right.toString() + " detpth: " + pair.left);
		}
		
		
		
		//testing Scanner Listener:
		/*
		stout.println("Waiting for exit string... ");
		while (true);
		*/
	}
	

	/*
	 * REALLY, A TEST METHOD
	 */
	private Vector<Pair<Integer, Object>> recieveObjects()
	{
		SequentialConnection<TP03Visitor> conn = PipeConn.createPipeline();
		ObjectHierarchyIterator ohi = new ObjectHierarchyIterator(conn, 0);
		Vector<Pair<Integer, Object>> collection = new Vector<Pair<Integer,Object>>();
		
		while (ohi.hasNext())
		{
			try
			{
				collection.add(ohi.next());
			}
			catch (Exception e)
			{
				stout.println("failed to fetch object.");
			}
		}
		try
		{
			conn.close();
		}
		catch (IOException e)
		{
			stout.println("failed to close pipeline");
		}
		return collection;
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
			stout.println("\n______________________________________");
			stout.println("\nClosing GenCon: " + message);
			if (PipeConn != null)
			{
				stout.print("Closing connection... ");
				PipeConn.close();
				stout.println("done.");
			}
			
			//if there is an exception, print trace if verbose debug mode is on.
			if (exc != null)
				Utils.PrintTraceIfDebug(exc, verboseDebugMode);
			
			
			
			// interrupting the ScannerListener.
			stout.print("Closing input listener... ");
			stin.close();
			stout.println("done.");
			
			stout.println("\nClean exit.");
			//closing standard out.
			stout.close();
			
			System.exit(exitType);
		}
		catch (Exception e)
		{
			stout.println("Error on exit. Quitting application.");
			Utils.PrintTraceIfDebug(e, verboseDebugMode);
			System.exit(ABNORMAL_EXIT);
		}
	}
	

	
	/**
	 * Used by the {@link ScannerListener} class, to notify the Client that
	 * the exit string has been encountered. 
	 * Not intended to be used otherwise.
	 */
	public void exitOnEncounteringExitString()
	{
		exit("Exit string '" + QUIT + "' encountered. Exiting Client...", NORMAL_EXIT, null);
	}
		
}