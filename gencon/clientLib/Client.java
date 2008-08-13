package gencon.clientLib;

import java.io.*;
import java.net.*;
import java.util.*;

import gencon.Master;
import gencon.Master.RULESET;
import gencon.clientLib.RFTS.ClientMethodsRFTS;
import gencon.clientLib.RISK.ClientMethodsRISK;
import gencon.utils.Utils;
import net.thousandparsec.netlib.*;
import net.thousandparsec.netlib.tp03.Board;
import net.thousandparsec.netlib.tp03.BoardIDs;
import net.thousandparsec.netlib.tp03.Design;
import net.thousandparsec.netlib.tp03.DesignIDs;
import net.thousandparsec.netlib.tp03.GetBoardIDs;
import net.thousandparsec.netlib.tp03.GetBoards;
import net.thousandparsec.netlib.tp03.GetDesignIDs;
import net.thousandparsec.netlib.tp03.GetMessage;
import net.thousandparsec.netlib.tp03.GetOrderDesc;
import net.thousandparsec.netlib.tp03.GetOrderDescIDs;
import net.thousandparsec.netlib.tp03.GetResource;
import net.thousandparsec.netlib.tp03.Order;
import net.thousandparsec.netlib.tp03.OrderDesc;
import net.thousandparsec.netlib.tp03.OrderDescIDs;
import net.thousandparsec.netlib.tp03.OrderParams;
import net.thousandparsec.netlib.tp03.ResourceIDs;
import net.thousandparsec.netlib.tp03.Response;
import net.thousandparsec.netlib.tp03.Sequence;
import net.thousandparsec.netlib.tp03.TP03Decoder;
import net.thousandparsec.netlib.tp03.TP03Visitor;
import net.thousandparsec.netlib.tp03.GetWithID.IdsType;
import net.thousandparsec.netlib.tp03.GetWithIDSlot.SlotsType;
import net.thousandparsec.netlib.tp03.IDSequence.ModtimesType;



/**
 * This is the basic client for GenCon. It complies with TP03 Protocol, 
 * and supports both RFTS and RISK games.
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
	//public final java.lang.Object END_OF_TURN_MONITOR = new java.lang.Object();
	private boolean turn_start_flag= false; //if true: client has not yet acted upon new turn. 
	
	//
	//	CONNECTION-RELATED
	//
	private URI serverURI;
	private ConnectionManager<TP03Visitor> connMgr;
	public final LoggerConnectionListener<TP03Visitor> EVENT_LOGGER;
	private final TP03Visitor VISITOR;
	private ClientMethods methods;

	//
	//	GAME-RELATED
	//

	
	
	
	/**
	 * The default constructor.
	 *
	 */
	public Client(Master master)
	{
		MASTER = master;
		EVENT_LOGGER = new LoggerConnectionListener<TP03Visitor>();
		VISITOR = new GCTP03Visitor(this);
	}
	
	/**
	 * Run this method to start the client.
	 * 
	 * @param args Necessary arguments. Refer to README for details.
	 */
	public void init(String[] args) throws IOException, TPException, IllegalArgumentException, EOFException, URISyntaxException
	{
		List<java.lang.Object> parsedArgs = Utils.parseArgs(args);
		
		MASTER.setVerboseDebugMode((Boolean) parsedArgs.get(4));
		MASTER.pl("Verbose debug mode on.");
		
		MASTER.setRuleset((RULESET)parsedArgs.get(0));
		MASTER.pl("Ruleset being played is: " + MASTER.getRuleset());
		
		//setting the correct connection methods:
		if (MASTER.getRuleset() == RULESET.RISK)
			methods = new ClientMethodsRISK(this);
		else
			methods = new ClientMethodsRFTS(this);
		//-----------------------------------------
		
		serverURI = new URI((String)parsedArgs.get(1));
		MASTER.pl("Server URI is: " + serverURI.toString());
		
		MASTER.setGenomeFileClasspath((String) parsedArgs.get(2));
		MASTER.pl("Genome file classpath is: " + MASTER.getGenomeFileClasspath());
		
		MASTER.setDifficulty((Short) parsedArgs.get(3));
		MASTER.pl("Difficulty set to: " + MASTER.getDifficulty());
		
		connect();
	}
	
	/*
	 * Establishes a pipelined connection with the server.
	 * Uses TP04 protocol classes.
	 * Autologin on/off, depends on the user
	 */
	private void connect() throws IOException, TPException
	{
		TP03Decoder decoder = new TP03Decoder();
		MASTER.pr("Establishing connection to server... ");
			
		Connection<TP03Visitor> basicCon = decoder.makeConnection(serverURI, true, VISITOR);
		basicCon.addConnectionListener(EVENT_LOGGER);
			
		connMgr = new ConnectionManager<TP03Visitor>(basicCon);
			
		MASTER.pl("connection established to : " + serverURI);
		MASTER.setMyUsername(Utils.getUsrnameFromURI(serverURI));
		MASTER.pl("Logged in successfully as : " + MASTER.getMyUsername());
			
		//testing!!!
		testMethods();
				
	}

	/**
	 * 
	 * @return A connection pipeline. Optimally, it should be closed after usage, but will otherwise close upon clean exit.
	 */
	public synchronized SequentialConnection<TP03Visitor> getPipeline()
	{
		return connMgr.createPipeline();
	}
	
	

	
	
	/**
	 * The {@link GCTP03Visitor} pushes this flag every time a turn starts.
	 * The {@link Master} pushes this flag back to original posotion, 
	 * once it's notified about turn start.
	 */
	public synchronized void pushTurnStartFlag()
	{
		turn_start_flag = !turn_start_flag;
		//END_OF_TURN_MONITOR.notify();
	}
	
	public synchronized boolean isTurnStart()
	{
		return turn_start_flag;
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
		getResourceDescs();
		//seeWhatsInside();
		//getDesigns();
		getOrdersDesc();
		showAllObjects();
		
		//throw new TPException("Tests finished.");
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
			net.thousandparsec.netlib.tp03.GetResourceIDs gri = new net.thousandparsec.netlib.tp03.GetResourceIDs();
			gri.setKey(-1);
			gri.setAmount(-1);
			ResourceIDs ris = conn.sendFrame(gri, ResourceIDs.class);
			
			pl(ris.toString());
			
			GetResource grs = new GetResource(); 
			List<IdsType> list = grs.getIds();
			for (int i = 0; i < ris.getModtimes().size(); i++)
				list.add(new IdsType(ris.getModtimes().get(i).getId()));
			
			net.thousandparsec.netlib.tp03.Sequence seq = conn.sendFrame(grs, Sequence.class);
			
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
	
	private void showAllObjects() throws IOException, TPException
	{
		SequentialConnection<TP03Visitor> conn = getPipeline();
		Collection<net.thousandparsec.netlib.tp03.Object> objects = ConnectionMethods.getAllObjects(conn);
		
		for (net.thousandparsec.netlib.tp03.Object obj : objects)
			pl(obj.toString());
		
		conn.close();
	}

	
	
	/////////////////////////////////////////////////////
	/////
	/////	END OF TEST METHODS
	/////
	/////////////////////////////////////////////////////
	
}