package gencon.clientLib;

import java.io.*;
import java.net.*;
import java.util.*;


import gencon.Master;
import gencon.gamelib.Game_Player;
import gencon.gamelib.gameobjects.Body;
import gencon.gamelib.gameobjects.Fleet;
import gencon.gamelib.gameobjects.FleetOrders;
import gencon.gamelib.gameobjects.StarSystem;
import gencon.gamelib.gameobjects.Universe;
import gencon.utils.*;
import net.thousandparsec.netlib.*;
import net.thousandparsec.netlib.tp03.*;
import net.thousandparsec.netlib.tp03.Object;
import net.thousandparsec.netlib.tp03.GetWithID.IdsType;
import net.thousandparsec.netlib.tp03.GetWithIDSlot.SlotsType;
import net.thousandparsec.netlib.tp03.IDSequence.ModtimesType;
import net.thousandparsec.netlib.tp03.Object.ContainsType;
import net.thousandparsec.netlib.tp03.ObjectParams.Fleet.ShipsType;
import net.thousandparsec.netlib.tp03.ObjectParams.Planet.ResourcesType;
import net.thousandparsec.util.Pair;


/**
 * This is the basic client for GenCon. As of now, it complies with TP03 Protocol.
 * Its sole functionality is to connect, log in, then send frames specified from outside,
 * and pass the received frames outside as well.
 * 
 * @author Victor Ivri
 *
 */
public class Client
{
	//
	//	MAINTANANCE
	//
	private final Master master;
	private boolean autorun;
	
	//
	//	CONNECTION-RELATED
	//
	private URI serverURI;
	private ConnectionManager<TP03Visitor> connMgr;
	public final LoggerConnectionListener<TP03Visitor> EVENT_LOGGER;
	private final TP03Visitor VISITOR;

	//game-related
	private String myUsername;
	private short difficulty;
	private String genomeFileClasspath;
	
	
	
	/**
	 * The default constructor.
	 *
	 */
	public Client(Master master)
	{
		this.master = master;
		EVENT_LOGGER = new LoggerConnectionListener<TP03Visitor>();
		VISITOR = new GCTP03Visitor();
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
		master.pl("Difficulty set to : " + difficulty);
		
		String URIstr = parsedArgs.right.left;
		if (!URIstr.equals(""))
			master.pl("URI set to : " + URIstr);
		
		
		genomeFileClasspath = parsedArgs.right.right;
		master.pl("Genotype File classpath set to : " + genomeFileClasspath);
		
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
		master.setVerboseDebugMode(Utils.setVerboseDebug());
		
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
		master.pl("Autorun mode. Initializing...");
		
		autorun = true;
		
		//verbose debug mode always true in autorun
		master.setVerboseDebugMode(true);
		
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
	 * Uses TP03 protocol classes.
	 * Autologin on/off, depends on the user
	 */
	private void connect() throws IOException, TPException
	{
		TP03Decoder decoder = new TP03Decoder();
		master.pr("Establishing connection to server... ");
			
			Connection<TP03Visitor> basicCon = decoder.makeConnection(serverURI, autorun, VISITOR);
			basicCon.addConnectionListener(EVENT_LOGGER);
			
			connMgr = new ConnectionManager<TP03Visitor>(basicCon);
			
			//PipelinedConnection<TP03Visitor> pConn = new PipelinedConnection<TP03Visitor>(basicCon);
			//this.PipeConn = pConn;
			
			if (autorun)
			{
				master.pl("connection established to : " + serverURI);
				myUsername = Utils.getUsrnameFromURI(serverURI);
				master.pl("Logged in successfully as : " + myUsername);
			}
			else //send connect frame...
			{
				SequentialConnection<TP03Visitor> conn = connMgr.createPipeline();
				Connect connect = new Connect();
				connect.setString("gencon-testing");
				conn.sendFrame(connect, Okay.class);
				conn.close();
				//if reach here, then ok.
				master.pl("connection established to : " + serverURI);
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
			SequentialConnection<TP03Visitor> conn = connMgr.createPipeline();
			master.pr("Logging in...");
			conn.sendFrame(loginFrame, Okay.class);
			conn.close();
			master.pl("Logged in successfully as : " + username);
		}
		catch (TPException tpe)
		{
			pl("Failed to login as user. Possible cause: username and password don't match. Try again.");
			Utils.PrintTraceIfDebug(tpe, master.isVerboseDebugMode());
			return false;
		}
		catch (IOException ioe)
		{
			pl("Unexpected failure. Failed to login. Try again.");
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
			pl("Failed to create new account. Possible cause: user already exists. Try again.");
			Utils.PrintTraceIfDebug(tpe, master.isVerboseDebugMode());
			return false;
		}
		catch (IOException ioe)
		{
			pl("Unexpected failure. Failed to login. Try again.");
			Utils.PrintTraceIfDebug(ioe, master.isVerboseDebugMode());
			return false;
		}
		return true;
		
	}

	
	/**
	 * 
	 * @return A connection pipeline. Optimally, it should be closed after usage, but will otherwise close upon clean exit.
	 */
	public synchronized SequentialConnection<TP03Visitor> getPipeline()
	{
		return connMgr.createPipeline();
	}
	
	
	public synchronized Object getObjectById(int id) throws IOException, TPException
	{
		SequentialConnection<TP03Visitor> conn = getPipeline();
		Object object = ConnectionMethods.getObjectById(conn, id);
		conn.close();
		return object;
	}
	
	public synchronized List<Body> getAllObjects() throws IOException, TPException
	{
		SequentialConnection<TP03Visitor> conn = getPipeline();
		Collection<Object> objects = ConnectionMethods.getAllObjects(conn);
		conn.close();
		
		List<Body> bodies = new ArrayList<Body>(objects.size());
		
		for (Object obj : objects)
		{
			if (obj != null)
			{
				int parent = -2;
				
				if (obj.getObject().getParameterType() == ObjectParams.Universe.PARAM_TYPE)
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
				for (ContainsType ct : obj.getContains())
					if (ct.getId() == child.getId())
						return obj;	
		
		//IF NOT FOUND:
		return null;
	}

	public synchronized int getTimeRemaining() throws IOException, TPException
	{
		SequentialConnection<TP03Visitor> conn = getPipeline();
		int time = ConnectionMethods.getTimeRemaining(conn);
		conn.close();
		return time;
	}
	
	public synchronized Game_Player getPlayerById(int id) throws IOException, TPException
	{
		SequentialConnection<TP03Visitor> conn = getPipeline();
		Player pl = ConnectionMethods.getPlayerById(id, conn);
		conn.close();
		
		return ObjectConverter.convertPlayer(pl);
	}

	public synchronized Collection<Game_Player> getAllPlayers(Collection<Body> game_objects) throws IOException, TPException
	{
		SequentialConnection<TP03Visitor> conn = getPipeline();
		Collection<Player> pls = ConnectionMethods.getAllPlayers(conn, game_objects);
		conn.close();
		
		Collection<Game_Player> players = new HashSet<Game_Player>();
		
		for (Player player : pls)
			players.add(ObjectConverter.convertPlayer(player));
		
		return players;
	}

	
	/**
	 * Returns the dimensions of the game-world in the following format:
	 * 
	 * @return Pair<sizeUnitsSquare, Pair<centerX, centerY>>
	 */
	public synchronized Pair<Long, Pair<Long, Long>> getUniverseDimensions() throws TPException, IOException
	{
		SequentialConnection<TP03Visitor> conn = getPipeline();
		Object o = ConnectionMethods.getObjectById(conn, 0);
		conn.close();
		
		Long x = new Long(o.getPos().getX());
		Long y = new Long(o.getPos().getY());
		Pair<Long, Long> universePos = new Pair<Long, Long>(x, y);
		
		Long size = new Long(o.getSize());
		
		return new Pair<Long, Pair<Long,Long>>(size, universePos);
	}

	/**
	 * Order a fleet to move to any star-system in the game-world.
	 * 
	 * @param fleet_id The fleet in question.
	 * @param destination_star_system The ultimate destination.
	 * @param urgent If true, then order will be placed in the beginning of the queue; if false, at the end.
	 * @return The number of turns for the order to complete, or -1 if it's an illegal order.
	 */
	public synchronized int moveFleet(Fleet fleet, StarSystem destination_star_system, boolean urgent) throws TPException, IOException
	{
		SequentialConnection<TP03Visitor> conn = getPipeline();
		int result = ConnectionMethods.orderMove(fleet.GAME_ID, destination_star_system.GAME_ID, urgent, conn);
		conn.close();
		return result;
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
			master.pr("Closing connection... ");
			connMgr.close();
			master.pl("done.");
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
		getDesigns();
	}
	
	
	
	/**
	 * REALLY, JUST A TEST METHOD TO MAP RESOURCE IDS TO RESOURCE TYPES.
	 * WILL GO AWAY WHEN I'M 101% SURE I DON'T NEED IT ANYMORE.
	 */
	public synchronized void getResourceDescs() 
	{
		try
		{
			SequentialConnection<TP03Visitor> conn = getPipeline();
			GetResourceIDs gri = new GetResourceIDs();
			gri.setKey(-1);
			gri.setAmount(-1);
			ResourceIDs ris = conn.sendFrame(gri, ResourceIDs.class);
			
			pl(ris.toString());
			
			GetResource grs = new GetResource(); 
			List<IdsType> list = grs.getIds();
			for (int i = 0; i < ris.getModtimes().size(); i++)
				list.add(new IdsType(ris.getModtimes().get(i).getId()));
			
			Sequence seq = conn.sendFrame(grs, Sequence.class);
			
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
		SequentialConnection<TP03Visitor> conn = getPipeline();
		List<Order> orders = ConnectionMethods.getOrdersForObject(conn, objectId, order_num);
		
		//just print them out for now, and see..
		pl("Orders for object: " + objectId);
		for (Order o : orders)
			if (o != null)
			{
					pl("--> " + o.toString() + " Details:\n------>");
					OrderDesc od = new OrderDesc();
					od.setId(o.getOtype());
					List<OrderParams> params = o.getOrderparams(od);
					for (OrderParams op : params)
					{
						OrderParams.OrderParamObject opo = (OrderParams.OrderParamObject) op;
						master.pr("Object: " + opo.getObjectid());
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
			SequentialConnection<TP03Visitor> conn = getPipeline();
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
	/*
	public void testMove()
	{
		int myId = 140;
		
		int dest1 = 115; //Procyon
		int dest2 = 55; //
		
		try
		{
			int turns1 = moveFleet(myId, dest1, false); //non-urgent
			int turns2 = moveFleet(myId, dest2, true); //urgent
			
			pl("Fleet move will take " + turns1 + " to destination1, and " + turns2 + " to destination2.");
		}
		catch (Exception e)
		{
			pl(e.getMessage());
		}
	}
	*/
	
	public Collection<Design> getDesigns() throws TPException, IOException
	{
		SequentialConnection<TP03Visitor> conn = getPipeline();
		Collection<Design> designs = ConnectionMethods.getDesigns(conn);
		conn.close();
		return designs;
	}
	
	
	public void seeWhatsInside() throws TPException, IOException
	{
		SequentialConnection<TP03Visitor> conn = getPipeline();
		/*
		Object o = getObjectById(146);
		
		ObjectParams.Fleet fl = (ObjectParams.Fleet)o.getObject();
		
		for (ShipsType st : fl.getShips())
			pl(st.toString());
		*/
		
		Object p = getObjectById(13);
		
		ObjectParams.Planet pl = (ObjectParams.Planet)p.getObject();
		
		for (ResourcesType rt : pl.getResources())
			pl(rt.toString());
		
		
		conn.close();
	}
	
	
	public void getMessages() throws IOException, TPException
	{
		SequentialConnection<TP03Visitor> conn = getPipeline();
		
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