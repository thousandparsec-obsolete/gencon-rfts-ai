package gencon.clientLib;

import java.io.*;
import java.net.*;
import java.util.*;

import gencon.Master;
import gencon.gamelib.FullGameStatus;
import gencon.gamelib.ObjectConverter;
import gencon.gamelib.gameobjects.Universe;
import gencon.utils.*;
import net.thousandparsec.netlib.*;
import net.thousandparsec.util.*;
import net.thousandparsec.netlib.tp03.*;
import net.thousandparsec.netlib.tp03.Object;
import net.thousandparsec.netlib.tp03.GetWithID.IdsType;
import net.thousandparsec.netlib.tp03.Object.ContainsType;


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
	//
	//	MAINTANANCE
	//
	private final Master<V> master;
	
	private static final PrintStream stout = System.out; 
	private boolean verboseDebugMode = true; // True by default.
	private boolean autorun;
	
	//
	//	CONNECTION-RELATED
	//
	private URI serverURI;
	private ConnectionManager<TP03Visitor> connMgr;
	private final LoggerConnectionListener<TP03Visitor> eventLogger;
	private final TP03Visitor visitor;


	//game-related
	private String myUsername;
	private short difficulty = 5;
	
	
	
	/**
	 * The default constructor.
	 *
	 */
	public Client(Master<V> master)
	{
		this.master = master;
		eventLogger = new LoggerConnectionListener<TP03Visitor>();
		visitor = new GCTP03Visitor();
	}
	
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
	public void runClient(String[] args) throws IOException, TPException, IllegalArgumentException, EOFException, URISyntaxException
	{
		String URIstr = "";
		
		if (args.length == 0) {/* NORMAL OPERATION OF CLIENT */}
		
		//configuring options for autorun.
		else if ((args.length == 2 || args.length == 3) && args[0].equals("-a"))
		{
			URIstr = args[1];
				if (args.length == 3)
				{
					difficulty = new Short(args[2]).shortValue();
					if (!(difficulty > 0 && difficulty < 10)) //difficulty between 1-9
						throw new IllegalArgumentException("Illegal Arguments. See documentation for proper syntax. Try again.");
				}
				stout.println("Difficulty set to " + difficulty);
					
		}
		else
			throw new IllegalArgumentException("Illegal Arguments. See documentation for proper syntax. Try again.");
		
		
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
	private void initNoAutorun() throws IOException, TPException, EOFException
	{
		autorun = false;
		
		stout.println("Follow the instructions...");
		//set verbose debug mode on/off
		master.setVerboseDebugMode(Utils.setVerboseDebug());
		
		//set URI
		serverURI = Utils.manualSetURI();
		
		// establish a connection with the server, no autologin.
		connect();

		//login as existing user, or create new user and then login
		loginOrCreateUser();
	}
	
	/*
	 * AUTORUN.
	 * Initializes client with previously specified URI string. Autologin enabled.
	 * @param URI {@link URI} string (with user info).
	 */
	private void initAutorun(String URIstring) throws IllegalArgumentException, IOException, TPException, URISyntaxException
	{
		stout.println("Autorun mode. Initializing...");
		
		autorun = true;
		
		//verbose debug mode always true in autorun
		master.setVerboseDebugMode(true);
		
		if (setURI(URIstring)) //if URI is valid, proceed with normal operation
		{
			serverURI = new URI(URIstring);
			connect();
		}
		 /*	
		    Throw exception otherwise. 
		 	The rationale is that autorun will most likely be part of a test suite, 
		 	and there will be no room for user corrections.
		 */
		else 
			throw new IllegalArgumentException("Invalid URI. Exiting autorun.");

	}
	
	/*
	 * Making the server URI from a string.
	 * Retrurns true if successful, false otherwise.
	 */
	private boolean setURI(String URIString)
	{
		URI uri = Utils.setURI(URIString);
		
		if (uri != null)
		{
			serverURI = uri;
			return true;
		}
		else
			return false;
	}

	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * 
	 *	CONNECTS.
	 * 
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 */


	/*
	 * Establishes a pipelined connection with the server.
	 * Uses TP03 protocol classes.
	 * Autologin on/off, depends on the user
	 */
	private void connect() throws IOException, TPException
	{
		TP03Decoder decoder = new TP03Decoder();
			stout.print("Establishing connection to server... ");
			
			Connection<TP03Visitor> basicCon = decoder.makeConnection(serverURI, autorun, visitor);
			basicCon.addConnectionListener(eventLogger);
			
			connMgr = new ConnectionManager<TP03Visitor>(basicCon);
			
			//PipelinedConnection<TP03Visitor> pConn = new PipelinedConnection<TP03Visitor>(basicCon);
			//this.PipeConn = pConn;
			
			if (autorun)
			{
				stout.println("connection established to : " + serverURI);
				myUsername = Utils.getUsrnameFromURI(serverURI);
				stout.println("Logged in successfully as : " + myUsername);
			}
			else //send connect frame...
			{
				SequentialConnection<TP03Visitor> conn = connMgr.createPipeline();
				Connect connect = new Connect();
				connect.setString("gencon-testing");
				conn.sendFrame(connect, Okay.class);
				conn.close();
				//if reach here, then ok.
				stout.println("connection established to : " + serverURI);
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
	private void loginOrCreateUser() throws EOFException
	{
		boolean retry = false;
		do 
		{
			stout.print("Create new account or login as existing user? (new / login) : ");
			String choose = Master.in.next();
		//	quitIfEncounterExitString(choose);
			
			if (choose.equals("login"))
			{
				String[] user = Utils.enterUserDetails();
				String username = user[0];
				String password = user[1];
				
				if (!login(username , password))
					retry = true;
				else
					retry = false;
			}
			else if (choose.equals("new"))
			{
				String[] user = Utils.enterUserDetails();
				String username = user[0];
				String password = user[1];
				if (createNewAccount(username, password))
				{
					if (!login(username, password))
					{
						stout.println("Unexpected failure to login after creating account. Try logging as the new user manually.");
						retry = true;
					}
					else
						retry = false;
				}
				else
				{
					stout.println("Failed to create account. Try again.");
					retry = true;
				}
			}
			else //other input
			{
				stout.println("Invalid input. Try again.");
				retry = true;
			}
		} while (retry);
	}
	

	
	private boolean login(String username, String password) throws EOFException 
	{
		Login loginFrame = new Login();
		loginFrame.setUsername(username);
		loginFrame.setPassword(password);
		
		try
		{
			//will be supplanted by the ThreadedPipelineManager methods... sometime... in the future...
			SequentialConnection<TP03Visitor> conn = connMgr.createPipeline();
			stout.print("Logging in...");
			conn.sendFrame(loginFrame, Okay.class);
			conn.close();
			stout.println("Logged in successfully as : " + username);
		}
		catch (TPException tpe)
		{
			stout.println("Failed to login as user. Possible cause: username and password don't match. Try again.");
			Utils.PrintTraceIfDebug(tpe, master.isVerboseDebugMode());
			return false;
		}
		catch (IOException ioe)
		{
			stout.println("Unexpected failure. Failed to login. Try again.");
			Utils.PrintTraceIfDebug(ioe, master.isVerboseDebugMode());
			return false;
		}
		return true;

	}
	
	private boolean createNewAccount(String username, String password) throws EOFException
	{
		
		CreateAccount newAccount = new CreateAccount();
		newAccount.setUsername(username);
		newAccount.setPassword(password);
		
		SequentialConnection<TP03Visitor> conn = connMgr.createPipeline();
		try
		{
			conn.sendFrame(newAccount, Okay.class);
			conn.close();
		}
		catch (TPException tpe)
		{
			stout.println("Failed to create new account. Possible cause: user already exists. Try again.");
			Utils.PrintTraceIfDebug(tpe, master.isVerboseDebugMode());
			return false;
		}
		catch (IOException ioe)
		{
			stout.println("Unexpected failure. Failed to login. Try again.");
			Utils.PrintTraceIfDebug(ioe, master.isVerboseDebugMode());
			return false;
		}
		return true;
		
	}

	
	/**
	 * 
	 * @return A connection pipeline
	 */
	public SequentialConnection<TP03Visitor> getPipeline()
	{
		return connMgr.createPipeline();
	}
	
	
	public String getPlayerName()
	{
		return myUsername;
	}
	
	public short getDifficulty()
	{
		return difficulty;
	}
	
	
	
	
	
	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * 
	 *	NOT NEEDED ANYMORE; WILL BE REMOVED WHEN ALL USEFUL INFO WILL BE EXTRACTED FROM ITS CODE!
	 * 
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 */
	private void startPlay()
	{
		stout.println("Starting to play game... ");
		
		//printing time!
		try
		{
			int time = ConnectionMethods.getTimeRemaining(connMgr.createPipeline());
			stout.println("Time remaining until end of turn : " + time);
		}
		catch (Exception e)
		{
			stout.println("Failed to retreive time.");
			Utils.PrintTraceIfDebug(e, master.isVerboseDebugMode());
		}
		/*
		//getting universe!
		Object un = null;
		try
		{
			un = ConnectionUtils.getUniverse(connMgr.createPipeline(), this);
		}
		catch (Exception e)
		{
			stout.println("Could not get universe. Exiting.");
			exit("Failed to instantiate game.", ABNORMAL_EXIT, e);
		}
		Universe universe = (Universe)ObjectConverter.ConvertToBody(un, Universe.UNIVERSE_PARENT, 
				connMgr.createPipeline());
		gameStatus = new FullGameStatus(difficulty, universe, myUsername);
		
		*/
		
		
		stout.println("Receiving all players...");
		Vector<Player> players;
		try
		{
			players = ConnectionMethods.getAllPlayers(connMgr.createPipeline());
			//printing players:
			for (Player pl : players)
				stout.println("Pl. num: " + pl.getId() + " Pl. name: " + pl.getName()); 
		}
		catch (Exception e)
		{
			stout.println("Failed to retreive players.");
			Utils.PrintTraceIfDebug(e, master.isVerboseDebugMode());
		}
		
		stout.println("Receiving all objects...");		
		try
		{
			Vector<Object> objects = ConnectionMethods.receiveAllObjects(connMgr.createPipeline());
			for (Object obj : objects)
				stout.println("--> " + obj.toString());
		}
		catch (Exception e)
		{
			stout.println("Failed to retreive players");
			Utils.PrintTraceIfDebug(e, master.isVerboseDebugMode());
		}
	}
	
	/**
	 * Closing connection, and exiting Client.
	 * 
	 * @param message Exit message.
	 */
	public synchronized void exit() throws Exception
	{
		if (connMgr != null)
		{
			stout.print("Closing connection... ");
			connMgr.close();
			stout.println("done.");
		}
	}
}