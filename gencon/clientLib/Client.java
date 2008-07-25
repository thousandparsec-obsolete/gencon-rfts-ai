package gencon.clientLib;

import java.io.*;
import java.net.*;
import java.util.*;

import gencon.Master;
import gencon.Master.RULESET;
import gencon.clientLib.RFTS.ClientMethodsRFTS;
import gencon.clientLib.RFTS.ObjectConverter;
import gencon.clientLib.RISK.ClientMethodsRISK;
import gencon.gamelib.AbstractGameObject;
import gencon.gamelib.Players.Game_Player;
import gencon.gamelib.RFTS.gameobjects.Body;
import gencon.utils.Utils;
import net.thousandparsec.netlib.*;
import net.thousandparsec.netlib.tp04.Board;
import net.thousandparsec.netlib.tp04.BoardIDs;
import net.thousandparsec.netlib.tp04.Design;
import net.thousandparsec.netlib.tp04.Game;
import net.thousandparsec.netlib.tp04.GetBoardIDs;
import net.thousandparsec.netlib.tp04.GetBoards;
import net.thousandparsec.netlib.tp04.GetGames;
import net.thousandparsec.netlib.tp04.GetMessage;
import net.thousandparsec.netlib.tp04.GetObjectDesc;
import net.thousandparsec.netlib.tp04.GetObjectDescIDs;
import net.thousandparsec.netlib.tp04.GetOrderDesc;
import net.thousandparsec.netlib.tp04.GetOrderDescIDs;
import net.thousandparsec.netlib.tp04.GetResource;
import net.thousandparsec.netlib.tp04.GetResourceIDs;
import net.thousandparsec.netlib.tp04.Object;
import net.thousandparsec.netlib.tp04.ObjectDesc;
import net.thousandparsec.netlib.tp04.ObjectDescIDs;
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
import net.thousandparsec.netlib.tp04.Sequence;


/**
 * This is the basic client for GenCon. It complies with TP04 Protocol, and supports both RFTS and RISK games.
 * Its sole functionality is to connect, log in, then send frames specified from outside,
 * and pass the received frames outside as well.
 * 
 * NOTE: Metaserver support currently lacking; the URI must be of the server on which the game will be running.
 *
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
	public final java.lang.Object END_OF_TURN_MONITOR = new java.lang.Object();
	
	//
	//	CONNECTION-RELATED
	//
	private URI serverURI;
	private ConnectionManager<TP04Visitor> connMgr;
	public final LoggerConnectionListener<TP04Visitor> EVENT_LOGGER;
	private final TP04Visitor VISITOR;
	private ClientMethods methods;

	//
	//	GAME-RELATED
	//
	private String myUsername;
	private short difficulty;
	private String genomeFileClasspath;
	private boolean turnStartFlag = false; //if true: client has not yet acted upon new turn. 
	
	
	
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
	public void init(String[] args) throws IOException, TPException, IllegalArgumentException, EOFException, URISyntaxException
	{
		List<java.lang.Object> parsedArgs = Utils.parseArgs(args);
		
		MASTER.setVerboseDebugMode((Boolean) parsedArgs.get(4));
		MASTER.pl("Verbose debug mode on.");
		
		MASTER.setRuleset((RULESET)parsedArgs.get(0));
		MASTER.pl("Ruleset to be played is: " + MASTER.getRuleset());
		
		//setting the correct connection methods:
		if (MASTER.getRuleset() == RULESET.RISK)
			methods = new ClientMethodsRISK(this);
		else
			methods = new ClientMethodsRFTS(this);
		//-----------------------------------------
		
		serverURI = new URI((String)parsedArgs.get(1));
		MASTER.pl("Server URI is: " + serverURI.toString());
		
		genomeFileClasspath = (String) parsedArgs.get(2);
		MASTER.pl("Genome file classpath is: " + genomeFileClasspath);
		
		difficulty = (Short) parsedArgs.get(3);
		MASTER.pl("Difficulty set to: " + difficulty);
		
		connect();
	}

/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * 
 *	INITIALIZATION: NORMAL OPERATION OR AUTORUN
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
			
		Connection<TP04Visitor> basicCon = decoder.makeConnection(serverURI, true, VISITOR);
		basicCon.addConnectionListener(EVENT_LOGGER);
			
		connMgr = new ConnectionManager<TP04Visitor>(basicCon);
			
		MASTER.pl("connection established to : " + serverURI);
		myUsername = Utils.getUsrnameFromURI(serverURI);
		MASTER.pl("Logged in successfully as : " + myUsername);
			
		//extract Game frame:
		GetGames gg = new GetGames();
		SequentialConnection<TP04Visitor> conn = getPipeline();
		Game game = conn.sendFrame(gg, Game.class);
		//if this is a metaserver with more than one game, a TPException will be thrown at this point.
		conn.close();
			
		//make sure the game is either RFTS or RISK:
		/////// NEED TO FIND OUT THE ACTUAL NAME OF RISK!
		if (!game.getRule().trim().equals("TP RFTS") || game.getRule().trim().equals("RISK"))
			throw new TPException("Attempted to connect to a game other than TP RFTS or RISK");
			
		//getting the turn number:
		List<ParametersType> params = game.getParameters();
			
		int turn = -3; //setting to an invalid value, which will still calculate the correct type of RFTS turn.
		for (ParametersType pt : params) //there must be a turn num!
			if (pt.getParamid() == Paramid.turn)
				turn = (byte)pt.getIntvalue();
		
		//setting the turn num:
		MASTER.setTurn(turn);
			
		MASTER.pl("Successfully connected to a valid TP RFTS game:" + game.getName() + " ; Current turn number: " + turn);
			
		//testing!!!
		printObjectDesc();
		//getOrdersDesc();
				
	}
	

	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * 
	 *	CONNECTION METHODS USED IN NORMAL OPERATION OF CLIENT.
	 * 
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 */

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
		try
		{
			net.thousandparsec.netlib.tp04.Object object = ConnectionMethods.getObjectById(conn, id);
			return object;
		}
		finally
		{
			conn.close();
		}
	}
	
	public synchronized int getTimeRemaining() throws IOException, TPException
	{
		SequentialConnection<TP04Visitor> conn = getPipeline();
		
		try
		{
			int time = ConnectionMethods.getTimeRemaining(conn);
			return time;
		}
		finally
		{
			conn.close();
		}
	}
	
	public Collection<Design> getDesigns() throws TPException, IOException
	{
		SequentialConnection<TP04Visitor> conn = getPipeline();
		
		try
		{
			Collection<Design> designs = ConnectionMethods.getDesigns(conn);
			return designs;
		}
		finally
		{
			conn.close();
		}
	}

	/**
	 * Tells the server that this client has finished doing all actions for this turn. 
	 */
	public void finishedTurn() throws TPException, IOException
	{
		SequentialConnection<TP04Visitor> conn = getPipeline();
		try
		{
			ConnectionMethods.finishedTurn(conn);
		}
		finally
		{
			conn.close();
		}
	}
	
	
	/**
	 * The {@link GCTP04Visitor} pushes this flag every time a turn starts.
	 * The {@link Master} pushes this flag back to original posotion, 
	 * once it's notified about turn start.
	 */
	public synchronized void pushTurnStartFlag()
	{
		END_OF_TURN_MONITOR.notify();
	}
	
	/**
	 * If it's true, means that a new turn has commenced. 
	 */
	public synchronized boolean getTurnStartFlag()
	{
		return turnStartFlag;
	}
	
	
	public synchronized ClientMethods getClientMethods()
	{
		return methods;
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
		//printObjectDesc();
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
				Frame f = conn.receiveFrame(Response.class);
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
	
	
	
	private void printObjectDesc() throws TPException, IOException
	{
		SequentialConnection<TP04Visitor> conn = getPipeline();
		
		GetObjectDescIDs godi = new GetObjectDescIDs();
		godi.setAmount(-1);
		godi.setKey(-1);
		
		ObjectDescIDs odi = conn.sendFrame(godi, ObjectDescIDs.class);
		GetObjectDesc god = new GetObjectDesc();
		
		String ids = "Ids : ";
		for (ModtimesType mdt : odi.getModtimes())
		{
			god.getIds().add(new IdsType(mdt.getId()));
			ids += mdt.getId();
		}
		
		pl(ids);

		Sequence seq = conn.sendFrame(god, Sequence.class);
		pl("num: " + seq.getNumber());
		for (int i = 0; i <seq.getNumber(); i++)
		{
			pl("?");
			ObjectDesc od = conn.receiveFrame(ObjectDesc.class);
			pl("Object: " + od.toString());
		}
		
		conn.close();
	}
	
	/////////////////////////////////////////////////////
	/////
	/////	END OF TEST METHODS
	/////
	/////////////////////////////////////////////////////
	
}