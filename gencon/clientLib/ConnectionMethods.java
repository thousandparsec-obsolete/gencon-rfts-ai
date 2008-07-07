package gencon.clientLib;

import gencon.gamelib.gameobjects.Body;
import gencon.gamelib.gameobjects.Fleet;
import gencon.gamelib.gameobjects.FleetOrders;
import gencon.gamelib.gameobjects.Planet;
import gencon.gamelib.gameobjects.StarSystem;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import net.thousandparsec.netlib.Frame;
import net.thousandparsec.netlib.SequentialConnection;
import net.thousandparsec.netlib.TPException;
import net.thousandparsec.netlib.tp03.Fail;
import net.thousandparsec.netlib.tp03.GetObjectIDs;
import net.thousandparsec.netlib.tp03.GetObjectsByID;
import net.thousandparsec.netlib.tp03.GetOrder;
import net.thousandparsec.netlib.tp03.GetOrderDesc;
import net.thousandparsec.netlib.tp03.GetPlayer;
import net.thousandparsec.netlib.tp03.GetTimeRemaining;
import net.thousandparsec.netlib.tp03.GetWithID;
import net.thousandparsec.netlib.tp03.Object;
import net.thousandparsec.netlib.tp03.ObjectIDs;
import net.thousandparsec.netlib.tp03.ObjectParams;
import net.thousandparsec.netlib.tp03.Okay;
import net.thousandparsec.netlib.tp03.Order;
import net.thousandparsec.netlib.tp03.OrderDesc;
import net.thousandparsec.netlib.tp03.OrderInsert;
import net.thousandparsec.netlib.tp03.OrderParams;
import net.thousandparsec.netlib.tp03.OrderProbe;
import net.thousandparsec.netlib.tp03.Player;
import net.thousandparsec.netlib.tp03.Response;
import net.thousandparsec.netlib.tp03.Sequence;
import net.thousandparsec.netlib.tp03.TP03Visitor;
import net.thousandparsec.netlib.tp03.TimeRemaining;
import net.thousandparsec.netlib.tp03.GetWithID.IdsType;
import net.thousandparsec.netlib.tp03.GetWithIDSlot.SlotsType;
import net.thousandparsec.netlib.tp03.IDSequence.ModtimesType;
import net.thousandparsec.netlib.tp03.Object.OrdertypesType;
import net.thousandparsec.netlib.tp03.OrderParams.OrderParamObject;

public class ConnectionMethods 
{
	private ConnectionMethods(){}	//dummy constructor: static class.
	//private final static PrintStream stout = System.out;
	
	public synchronized static int getTimeRemaining(SequentialConnection<TP03Visitor> conn) throws IOException, TPException
	{
     	TimeRemaining tr = conn.sendFrame(new GetTimeRemaining(), net.thousandparsec.netlib.tp03.TimeRemaining.class);
		return tr.getTime();
	}

	public synchronized static Object getObjectById(SequentialConnection<TP03Visitor> conn, int id) throws IOException, TPException
	{
		GetObjectsByID get = new GetObjectsByID();
		get.getIds().add(new IdsType(id));
		
		conn.sendFrame(get, Sequence.class);
		Object object = conn.receiveFrame(Object.class);

		return object;
	}
	
	public synchronized static Vector<Object> getAllObjects(SequentialConnection<TP03Visitor> conn) throws IOException, TPException
	{
		GetObjectIDs gids = new GetObjectIDs();
		//sets 'gids' to receive all objects:
		gids.setKey(-1);
		gids.setAmount(-1);
	
		//receiving:
		ObjectIDs oids = conn.sendFrame(gids, ObjectIDs.class);
		List<ModtimesType> list = oids.getModtimes();
		
		//preparing the frame to receive objects:
		GetObjectsByID getObj = new GetObjectsByID();
		for (ModtimesType mdt : list)
			getObj.getIds().add(new IdsType(mdt.getId()));
		
		//receiving the sequence:
		Sequence seq = conn.sendFrame(getObj, Sequence.class);
		
		//preparing to store objects:
		Vector<Object> objects = new Vector<Object>();
		
		//receiving objects:
		for (int i = 0; i < seq.getNumber(); i++)
		{
			Object o = conn.receiveFrame(Object.class);
			objects.add(o);
			
			List<OrdertypesType> otypes = o.getOrdertypes();
		//	System.out.println("Object: " + o.getName() + " id: " + o.getId() + " Num of orders: " + o.getOrders() + " Ordertypes: " + otypes);
		}
		
		return objects;
	}

	public synchronized static Player getPlayerById(int id, SequentialConnection<TP03Visitor> conn) throws IOException, TPException
	{
		GetPlayer get = new GetPlayer();
		get.getIds().add(new IdsType(id));
		return conn.sendFrame(get, Player.class);
	}
	
	public synchronized static Vector<Player> getAllPlayers(SequentialConnection<TP03Visitor> conn, List<Body> game_objects) throws IOException, TPException
	{
		Vector<Integer> playerIds = new Vector<Integer>(game_objects.size());
		
		final int NEUTRAL = -1; //the standard demarcation of neutral objects.
		
		//add to list of ids if object is a fleet or non-neutral planet, unless the id has been encountered already.
		for (Body obj : game_objects)
			if (obj != null)
			{
				if (obj.TYPE == Body.BodyType.FLEET)
				{
					int owner = ((Fleet) obj).OWNER;
					if (!checkIfInList(playerIds, owner)) //if the id hasn't been encountered yet!
						playerIds.add(new Integer(owner));
				}
				else if (obj.TYPE == Body.BodyType.PLANET)
				{
					int owner = ((Planet) obj).OWNER;
					if (owner != NEUTRAL && !checkIfInList(playerIds, owner))
						playerIds.add(new Integer(owner));
				}
			}
		
		
		//THEN, RETREIVE ALL PLAYER FRAMES, BASED ON THAT LIST:
		Vector<Player> players = new Vector<Player>();
		for (Integer id : playerIds)
			if (id != null)
				players.add(getPlayerById(id, conn));

		return players;
	}
	
	private synchronized static boolean checkIfInList(List<Integer> list, int owner)
	{
		for (Integer i : list)
			if (i != null && i.intValue() == owner)
				return true;
		
		//in case not found!
		return false;
	}
	
	
	
	public synchronized static Vector<Order> getOrdersForObject(SequentialConnection<TP03Visitor> conn, int objectId, int order_quantity) throws TPException, IOException
	{
		GetOrder getOrd = new GetOrder();
		
		getOrd.setId(objectId);
		
		//getting the list of slots on the "Get Order" frame:
		List<SlotsType> slots = getOrd.getSlots();
		
		//adding slots, for which I want to recieve orders:
		for (int i = 0; i < order_quantity; i++) 
			slots.add(new SlotsType(i));
		
		//getting a sequence:
		Sequence seq = conn.sendFrame(getOrd, Sequence.class);
		
		//receiving orders:
		Vector<Order> orders = new Vector<Order>(seq.getNumber());
		for (int i = 0; i < seq.getNumber(); i++)
			orders.add(conn.receiveFrame(Order.class));
		
		return orders;
	}
		
	/**
	 * Order a fleet to move to any star-system in the game-world.
	 * 
	 * @param fleet_id The fleet in question.
	 * @param destination_star_system The ultimate destination.
	 * @param urgent If true, then order will be placed in the beginning of the queue; if false, at the end.
	 * @return The number of turns for the order to complete, or -1 if it's a bad order.
	 */
	public synchronized static int orderMove(int fleet_id, int destination_star_system, boolean urgent, SequentialConnection<TP03Visitor> conn) throws IOException, TPException
	{
		OrderInsert order = new OrderInsert();
		order.setOtype(FleetOrders.MOVE_ORDER); //the type of the order
		order.setId(fleet_id); //the object at hand.
		
		if (!urgent)
			order.setSlot(-1); //sets the location of the order at the end of the queue.
		else
			order.setSlot(0); //sets the location of the order at the beginning of the queue.
		
		//setting destination:
		OrderParams.OrderParamObject destination_param = new OrderParams.OrderParamObject();
		//destination_param.setObjectid(destination_star_system.GAME_ID);
		destination_param.setObjectid(destination_star_system); //for testing.
		
		
		//setting the parameters:
		List<OrderParams> op = new ArrayList<OrderParams>(1);
		op.add(destination_param);
		order.setOrderparams(op, getODbyId(order.getOtype(), conn)); 
		
		//getting the response:
		Response response = conn.sendFrame(order, Response.class);
		
		//if the order is legal, probe for the amount of turns:
		if (response.getFrameType() == Okay.FRAME_TYPE)
			return orderProbeGetTurns(order, FleetOrders.MOVE_ORDER, conn); 
		
		//if order illegal.
		else if (response.getFrameType() == Fail.FRAME_TYPE) 
			return -1;
		
		else //unexpected frame.
			throw new TPException("Unexpected frame while trying to insert move order.");
		
	}
	
	
	/*
	 * Probes the server, and returns the amount of turns to complete the order,
	 * or -1 if it's illegal, or else a TPException for anything else (which is unexpected).
	 * 
	 * The OrderDesc needs to be already set to the order type, to serve as the template for OrderParams.
	 */
	private synchronized static int orderProbeGetTurns(Order order, int order_id, SequentialConnection<TP03Visitor> conn) throws IOException, TPException
	{
		OrderProbe probe = new OrderProbe();
		
		//setting values:
		probe.setId(order.getId());
		probe.setOtype(order.getOtype());
		probe.setSlot(order.getSlot());
		
		//retrieving template and setting the order params:
		OrderDesc od = getODbyId(order_id, conn);
		probe.setOrderparams(order.getOrderparams(od), od);
		
		//probling:
		Response whatIf = conn.sendFrame(probe, Response.class);
		
		if (whatIf.getFrameType() == Order.FRAME_TYPE)
			return ((Order) whatIf).getTurns();
		
		else if (whatIf.getFrameType() == Fail.FRAME_TYPE)
			return -1;
		
		else 
			throw new TPException("Unexpected frame while probing order.");
	}
	
	private static synchronized OrderDesc getODbyId(int id, SequentialConnection<TP03Visitor> conn) throws TPException, IOException
	{
		GetOrderDesc god = new GetOrderDesc(); 
		List<IdsType> list = god.getIds();
		list.add(new IdsType(id));
		
		return conn.sendFrame(god, OrderDesc.class); 
	}
}
