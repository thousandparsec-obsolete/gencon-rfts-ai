package gencon.clientLib;

import java.io.*;
import java.net.*;
import java.util.*;

import gencon.Master;
import gencon.gamelib.Game_Player;
import gencon.gamelib.gameobjects.Body;
import gencon.gamelib.gameobjects.Fleet;
import gencon.gamelib.gameobjects.StarSystem;
import gencon.gamelib.gameobjects.Universe;
import gencon.utils.*;
import net.thousandparsec.netlib.*;
import net.thousandparsec.netlib.tp04.Board;
import net.thousandparsec.netlib.tp04.BoardIDs;
import net.thousandparsec.netlib.tp04.Connect;
import net.thousandparsec.netlib.tp04.Design;
import net.thousandparsec.netlib.tp04.FinishedTurn;
import net.thousandparsec.netlib.tp04.Game;
import net.thousandparsec.netlib.tp04.GetBoardIDs;
import net.thousandparsec.netlib.tp04.GetBoards;
import net.thousandparsec.netlib.tp04.GetGames;
import net.thousandparsec.netlib.tp04.GetMessage;
import net.thousandparsec.netlib.tp04.GetOrderDesc;
import net.thousandparsec.netlib.tp04.GetOrderDescIDs;
import net.thousandparsec.netlib.tp04.GetResource;
import net.thousandparsec.netlib.tp04.GetResourceIDs;
import net.thousandparsec.netlib.tp04.Object;
import net.thousandparsec.netlib.tp04.CreateAccount;
import net.thousandparsec.netlib.tp04.Login;
import net.thousandparsec.netlib.tp04.ObjectParams;
import net.thousandparsec.netlib.tp04.Okay;
import net.thousandparsec.netlib.tp04.Order;
import net.thousandparsec.netlib.tp04.OrderDesc;
import net.thousandparsec.netlib.tp04.OrderDescIDs;
import net.thousandparsec.netlib.tp04.OrderParams;
import net.thousandparsec.netlib.tp04.Player;
import net.thousandparsec.netlib.tp04.ResourceIDs;
import net.thousandparsec.netlib.tp04.Response;
import net.thousandparsec.netlib.tp04.TP04Decoder;
import net.thousandparsec.netlib.tp04.TP04Visitor;
import net.thousandparsec.netlib.tp04.Game.ParametersType;
import net.thousandparsec.netlib.tp04.Game.ParametersType.Paramid;
import net.thousandparsec.netlib.tp04.GetWithID.IdsType;
import net.thousandparsec.netlib.tp04.GetWithIDSlot.SlotsType;
import net.thousandparsec.netlib.tp04.IDSequence.ModtimesType;
import net.thousandparsec.netlib.tp04.Object.ContainsType;
import net.thousandparsec.netlib.tp04.Sequence;
import net.thousandparsec.util.Pair;


/**
 * This is the basic client for GenCon. It complies with TP04 Protocol.
 * Its sole functionality is to connect, log in, then send frames specified from outside,
 * and pass the received frames outside as well.
 * 
 * NOTE: Metaserver support currently lacking; the URI must be of the server on which the game will be running.
 * 
 * @author Victor Ivri
 *
 */
public class Client
{
	//
	//	MAINTANANCE
	//
	private final Master MASTER;
	private boolean autorun;
	
	//
	//	CONNECTION-RELATED
	//
	private URI serverURI;
	private ConnectionManager<TP04Visitor> connMgr;
	public final LoggerConnectionListener<TP04Visitor> EVENT_LOGGER;
	private final TP04Visitor VISITOR;

	//game-related
	private String myUsername;
	private short difficulty;
	private String genomeFileClasspath;
	private boolean turnStartFlag;
	
	
	
	/**
	 * The default constructor.
	 *
	 */
	public Client(Master master)
	{
		MASTER = master;
		EVENT_LOGGER = new LoggerConnectionListener<TP04Visitor>();
		VISITOR = new GCTP04Visitor(this);
	}
	
	/**
	 * Run this method to start the client.
	 * 
	 * @param args Optional arguments. Please see README for details.
	 */
	public void runClient(String[] args) throws IOException, TPException, IllegalArgumentException, EOFException, URISyntaxException
	{
		Pair<Short, Pair<String, String>> parsedArgs = Utils.parseArgs(args);
		
		difficulty = parsedArgs.left.shortValue();
		MASTER.pl("Difficulty set to : " + difficulty);
		
		String URIstr = parsedArgs.right.left;
		if (!URIstr.equals(""))
			MASTER.pl("URI set to : " + URIstr);
		
		
		genomeFileClasspath = parsedArgs.right.right;
		MASTER.pl("Genotype File classpath set to : " + genomeFileClasspath);
		
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
	 * Initializes client. All relevant parameters set by user through standard input.
	 * No autologin.
	 */
	private void initNoAutorun() throws IOException, TPException, EOFException
	{
		autorun = false;
		
		pl("Follow the instructions... (input is CAPSLOCK sensitive)");
		//set verbose debug mode on/off
		MASTER.setVerboseDebugMode(Utils.setVerboseDebug());
		
		//set URI
		serverURI = Utils.manualSetURI();
		
		//set genotype file classpath:
		genomeFileClasspath = Utils.manualSetGenomeClasspath();
		
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
		MASTER.pl("Autorun mode. Initializing...");
		
		autorun = true;
		
		//verbose debug mode always true in autorun
		MASTER.setVerboseDebugMode(true);
		
		if (setURI(URIstring)) //Set URI. If URI is valid, proceed with normal operation.
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
	 * Uses TP04 protocol classes.
	 * Autologin on/off, depends on the user
	 */
	private void connect() throws IOException, TPException
	{
		TP04Decoder decoder = new TP04Decoder();
		MASTER.pr("Establishing connection to server... ");
			
			Connection<TP04Visitor> basicCon = decoder.makeConnection(serverURI, autorun, VISITOR);
			basicCon.addConnectionListener(EVENT_LOGGER);
			
			connMgr = new ConnectionManager<TP04Visitor>(basicCon);
			
			if (autorun)
			{
				MASTER.pl("connection established to : " + serverURI);
				myUsername = Utils.getUsrnameFromURI(serverURI);
				MASTER.pl("Logged in successfully as : " + myUsername);
			}
			else //send connect frame...
			{
				SequentialConnection<TP04Visitor> conn = getPipeline();
				Connect connect = new Connect();
				connect.setString("gencon-testing");
				conn.sendFrame(connect, Okay.class);
				conn.close();
				//if reach here, then ok.
				MASTER.pl("connection established to : " + serverURI);
			}
			
			//extract Game frame:
			GetGames gg = new GetGames();
			SequentialConnection<TP04Visitor> conn = getPipeline();
			Game game = conn.sendFrame(gg, Game.class);
			//if this is a metaserver with more than one game, a TPException will be thrown at this point.
			conn.close();
			
			//make sure the game is RFTS:
			if (!game.getRule().trim().equals("TP RFTS"))
				throw new TPException("Attempted to connect to a game other than TP RFTS");
			
			//getting the turn number:
			List<ParametersType> params = game.getParameters();
			
			int turn = -3; //setting to an invalid value, which will still calculate the correct type of RFTS turn.
			for (ParametersType pt : params) //must be a turn num!
				if (pt.getParamid() == Paramid.turn)
					turn = (byte)pt.getIntvalue();
			//setting the turn num:
			MASTER.setTurn(turn);
			
			MASTER.pl("Successfully connected to a valid TP RFTS game:" + game.getName() + " ; Current turn number: " + turn);
				
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
	 */
	private void loginOrCreateUser() throws EOFException
	{
		boolean retry = false;
		do 
		{
			pr("Create new account or login as existing user? (new / login) : ");
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
						pl("Unexpected failure to login after creating account. Try logging as the new user manually.");
						retry = true;
					}
					else
						retry = false;
				}
				else
				{
					pl("Failed to create account. Try again.");
					retry = true;
				}
			}
			else //other input
			{
				pl("Invalid input. Try again.");
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
			SequentialConnection<TP04Visitor> conn = connMgr.createPipeline();
			MASTER.pr("Logging in...");
			conn.sendFrame(loginFrame, Okay.class);
			conn.close();
			MASTER.pl("Logged in successfully as : " + username);
		}
		catch (TPException tpe)
		{
			pl("Failed to login as user. Possible cause: username and password don't match. Try again.");
			Utils.PrintTraceIfDebug(tpe, MASTER.isVerboseDebugMode());
			return false;
		}
		catch (IOException ioe)
		{
			pl("Unexpected failure. Failed to login. Try again.");
			Utils.PrintTraceIfDebug(ioe, MASTER.isVerboseDebugMode());
			return false;
		}
		return true;

	}
	
	private boolean createNewAccount(String username, String password) throws EOFException
	{
		
		CreateAccount newAccount = new CreateAccount();
		newAccount.setUsername(username);
		newAccount.setPassword(password);
		
		SequentialConnection<TP04Visitor> conn = connMgr.createPipeline();
		try
		{
			conn.sendFrame(newAccount, Okay.class);
			conn.close();
		}
		catch (TPException tpe)
		{
			pl("Failed to create new account. Possible cause: user already exists. Try again.");
			Utils.PrintTraceIfDebug(tpe, MASTER.isVerboseDebugMode());
			return false;
		}
		catch (IOException ioe)
		{
			pl("Unexpected failure. Failed to login. Try again.");
			Utils.PrintTraceIfDebug(ioe, MASTER.isVerboseDebugMode());
			return false;
		}
		return true;
		
	}

	
	/**
	 * 
	 * @return A connection pipeline. Optimally, it should be closed after usage, but will otherwise close upon clean exit.
	 */
	public synchronized SequentialConnection<TP04Visitor> getPipeline()
	{
		return connMgr.createPipeline();
	}
	
	
	public synchronized Object getObjectById(int id) throws IOException, TPException
	{
		SequentialConnection<TP04Visitor> conn = getPipeline();
		net.thousandparsec.netlib.tp04.Object object = ConnectionMethods.getObjectById(conn, id);
		conn.close();
		return object;
	}
	
	public synchronized List<Body> getAllObjects() throws IOException, TPException
	{
		SequentialConnection<TP04Visitor> conn = getPipeline();
		Collection<Object> objects = ConnectionMethods.getAllObjects(conn);
		conn.close();
		
		List<Body> bodies = new ArrayList<Body>(objects.size());
		
		for (Object obj : objects)
		{
			if (obj != null)
			{
				int parent = -2;
				
				if (obj.getObject().getParameterType() == ObjectParams..PARAM_TYPE)
					parent = Universe.UNIVERSE_PARENT;
				else
					parent = findParent(objects, obj).getId(); 
				//if it's not a universe, it must have a parent! If rule broken, null pointer will be thrown.
			
					bodies.add(ObjectConverter.convertToBody(obj, parent, this));
			}
		}
		
		return bodies;
	}

	/*
	 * Helper method for receiveAllObjects().
	 * Returns the immediate parent of the object
	 */
	private Object findParent(Collection<Object> objects, Object child)
	{
		for (Object obj : objects)
			if (obj != null)
				for (ContainsType ct : obj())
					if (ct.getId() == child.())
						return obj;	
		
		//IF NOT FOUND:
		return null;
	}

	public synchronized int getTimeRemaining() throws IOException, TPException
	{
		SequentialConnection<TP04Visitor> conn = getPipeline();
		int time = ConnectionMethods.getTimeRemaining(conn);
		conn.close();
		return time;
	}
	
	public synchronized Game_Player getPlayerById(int id) throws IOException, TPException
	{
		SequentialConnection<TP04Visitor> conn = getPipeline();
		Player pl = ConnectionMethods.getPlayerById(id, conn);
		conn.close();
		
		return ObjectConverter.convertPlayer(pl);
	}

	/**
	 * Order a fleet to move to any star-system in the game-world.
	 * 
	 * @param fleet_id The fleet in question.
	 * @param destination_star_system The ultimate destination.
	 * @param urgent If true, then order will be placed in the beginning of the queue; if false, at the end.
	 * @return The number of turns for the order to complete, or -1 if it's an illegal order.
	 */
	public synchronized boolean moveFleet(Fleet fleet, StarSystem destination_star_system, boolean urgent) throws TPException, IOException
	{
		SequentialConnection<TP04Visitor> conn = getPipeline();
		boolean result = ConnectionMethods.orderMove(fleet, destination_star_system, urgent, conn);
		conn.close();
		return result;
	}

	public synchronized Collection<Game_Player> getAllPlayers(Collection<Body> game_objects) throws IOException, TPException
	{
		SequentialConnection<TP04Visitor> conn = getPipeline();
		Collection<Player> pls = ConnectionMethods.getAllPlayers(conn, game_objects);
		conn.close();
		
		Collection<Game_Player> players = new HashSet<Game_Player>();
		
		for (Player player : pls)
			players.add(ObjectConverter.convertPlayer(player));
		
		return players;
	}

	
	public Collection<Design> getDesigns() throws TPException, IOException
	{
		SequentialConnection<TP04Visitor> conn = getPipeline();
		Collection<Design> designs = ConnectionMethods.getDesigns(conn);
		conn.close();
		return designs;
	}

	/**
	 * Tells the server that this client has finished doing all actions for this turn. 
	 */
	public void finishedTurn() throws TPException, IOException
	{
		SequentialConnection<TP04Visitor> conn = getPipeline();
		conn.sendFrame(new FinishedTurn(), Response.class);
		conn.close();
	}
	
	
	/**
	 * The {@link GCTP04Visitor} pushes this flag every time a turn starts.
	 * The {@link Master} pushes this flag back to original posotion, 
	 * once it's notified about turn start.
	 */
	public synchronized void pushTurnStartFlag()
	{
		turnStartFlag = !turnStartFlag;
	}
	
	/**
	 * If it's true, means that a new turn has commenced. 
	 */
	public synchronized boolean getTurnStartFlag()
	{
		return turnStartFlag;
	}
	
	/**
	 * Closing connection.
	 * 
	 * @param message Exit message.
	 */
	public synchronized void exit() throws Exception
	{
		if (connMgr != null)
		{
			MASTER.pr("Closing connection... ");
			connMgr.close();
			MASTER.pl("done.");
		}
	}
	
	
	public String getGenomeFileClasspath()
	{
		return genomeFileClasspath;
	}
	
	public synchronized short getDifficulty()
	{
		return difficulty;
	}

	public synchronized String getPlayerName()
	{
		return myUsername;
	}

	//std in/out that's not dependent on verbose debug mode:
	private void pl(String st)
	{
		System.out.println(st);
	}
	
	private void pr(String st)
	{
		System.out.print(st);
	}
	
	
	
	/////////////////////////////////////////////////////
	/////
	/////	TEST METHODS
	/////
	/////////////////////////////////////////////////////
	/**
	 * RUNS THE TEST METHODS NEEDED IN THE GIVEN MOMENT
	 *
	 */
	public void testMethods() throws IOException, TPException
	{
		//getResourceDescs();
		//seeWhatsInside();
		//getDesigns();
		//getOrdersDesc();
	}
	
	
	
	/**
	 * REALLY, JUST A TEST METHOD TO MAP RESOURCE IDS TO RESOURCE TYPES.
	 * WILL GO AWAY WHEN I'M 101% SURE I DON'T NEED IT ANYMORE.
	 */
	public synchronized void getResourceDescs() 
	{
		try
		{
			SequentialConnection<TP04Visitor> conn = getPipeline();
			GetResourceIDs gri = new GetResourceIDs();
			gri.setKey(-1);
			gri.setAmount(-1);
			ResourceIDs ris = conn.sendFrame(gri, ResourceIDs.class);
			
			pl(ris.toString());
			
			GetResource grs = new GetResource(); 
			List<IdsType> list = grs.getIds();
			for (int i = 0; i < ris.getModtimes().size(); i++)
				list.add(new IdsType(ris.getModtimes().get(i).getId()));
			
			net.thousandparsec.netlib.tp04.Sequence seq = conn.sendFrame(grs, Sequence.class);
			
			for (int i = 0; i < seq.getNumber(); i++)
			{
				Frame f = conn.receiveFrame(Frame.class);
				pl("Resource: " + f.toString());
			}
		}
		catch (Exception e)
		{
			pl("Failed to retreive resources.");
			e.printStackTrace();
			return;
		}
		
		
	}

	public void getOrdersForMyObjects()
	{
		try
		{
			getOrdersForObject(139, 3);
		}
		catch (Exception e)
		{
			pl("unsuccessful. " + e.getMessage());
		}
	}

	//VOID FOR NOW... WANT TO SEE HOW IT PRINTS THEM OUT.
	public void getOrdersForObject(int objectId, int order_num) throws TPException, IOException
	{
		SequentialConnection<TP04Visitor> conn = getPipeline();
		List<Order> orders = ConnectionMethods.getOrdersForObject(conn, objectId, order_num);
		
		//just print them out for now, and see..
		pl("Orders for object: " + objectId);
		for (Order o : orders)
			if (o != null)
			{
					pl("--> " + o.toString() + " Details:\n------>");
					OrderDesc od = new OrderDesc();
					od.setId(o.getOtype());
					List<OrderParams> params = o.getParameters(od);
					for (OrderParams op : params)
					{
						OrderParams.OrderParamObject opo = (OrderParams.OrderParamObject) op;
						MASTER.pr("Object: " + opo.getObjectid());
						OrderParams.OrderParamString ops = (OrderParams.OrderParamString) op;
						pr("  Destination : " + opo.getObjectid() + "\n");
					}
			}
		conn.close();
	}

	/**
	 * REALLY, JUST A TEST METHOD TO EXAMINE ORDERS.
	 * WILL GO AWAY WHEN I'M 101% SURE I DON'T NEED IT ANYMORE.
	 */
	public void getOrdersDesc()
	{
		try
		{
			SequentialConnection<TP04Visitor> conn = getPipeline();
			GetOrderDescIDs ged = new GetOrderDescIDs();
			ged.setKey(-1);
			ged.setAmount(-1);
			OrderDescIDs ris = conn.sendFrame(ged, OrderDescIDs.class);
			
			pl(ris.toString());
			
			GetOrderDesc grs = new GetOrderDesc(); 
			List<IdsType> list = grs.getIds();
			for (int i = 0; i < ris.getModtimes().size(); i++)
				list.add(new IdsType(ris.getModtimes().get(i).getId()));
			
			Sequence seq = conn.sendFrame(grs, Sequence.class);
			
			for (int i = 0; i < seq.getNumber(); i++)
			{
				OrderDesc od = conn.receiveFrame(OrderDesc.class);
				pl("Order: " + od.toString());
			}
		}
		catch (Exception e)
		{
			pl("Failed to retreive orders.");
			e.printStackTrace();
			return;
		}
		
		
		
		
	}

	
	public void getMessages() throws IOException, TPException
	{
		SequentialConnection<TP04Visitor> conn = getPipeline();
		
		GetBoardIDs gbids = new GetBoardIDs();
		gbids.setAmount(-1);
		gbids.setKey(-1);
		BoardIDs bids = conn.sendFrame(gbids, BoardIDs.class);
		
		GetBoards gb = new GetBoards();
		for(ModtimesType mdt : bids.getModtimes())
			gb.getIds().add(new IdsType(mdt.getId()));
		
		Board b = conn.sendFrame(gb, Board.class);
		pl(b.toString());
		
		
		GetMessage gm = new GetMessage();
		gm.setId(b.getId());
		List<SlotsType> slots = gm.getSlots();
		for (int i = 0; i < b.getMessages(); i ++)
		{
			////// will complete a bit later :)
		}
		conn.close();
	}
	
	/////////////////////////////////////////////////////
	/////
	/////	END OF TEST METHODS
	/////
	/////////////////////////////////////////////////////
	
}