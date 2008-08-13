package gencon.clientLib.RFTS;

import gencon.clientLib.Client;
import gencon.clientLib.ClientMethods;
import gencon.clientLib.ConnectionMethods;
import gencon.gamelib.Players.Game_Player;
import gencon.gamelib.RFTS.gameobjects.Body;
import gencon.gamelib.RFTS.gameobjects.Fleet;
import gencon.gamelib.RFTS.gameobjects.Orders;
import gencon.gamelib.RFTS.gameobjects.Planet;
import gencon.gamelib.RFTS.gameobjects.StarSystem;
import gencon.gamelib.RFTS.gameobjects.Universe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import net.thousandparsec.netlib.SequentialConnection;
import net.thousandparsec.netlib.TPException;
import net.thousandparsec.netlib.tp03.*;
import net.thousandparsec.netlib.tp03.Object;
import net.thousandparsec.netlib.tp03.Object.ContainsType;


public class ClientMethodsRFTS extends ClientMethods
{
	public ClientMethodsRFTS(Client client)
	{
		super(client);
	}

	public synchronized Collection<Body> getAllBodies() throws IOException, TPException
	{
		Collection<Object> objects = getAllObjects();
		
		return convertObjectsToBodies(objects);
	}
	
	
	public synchronized Collection<Body> convertObjectsToBodies(Collection<Object> objects) throws IOException, TPException
	{
		Collection<Body> bodies = new HashSet<Body>(objects.size());
		
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
			
					bodies.add(ObjectConverter.convertToBody(obj, parent));
			}
		}
		
		return bodies;
	}

	/*
	 * Helper method for convertObjectsToBodies().
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
	
	public synchronized Collection<Game_Player> getAllPlayers(Collection<Object> game_objects) throws IOException, TPException
	{
		SequentialConnection<TP03Visitor> conn = CLIENT.getPipeline();
		
		try
		{
			Collection<Player> pls = ConnectionMethods.getAllPlayers(conn, game_objects);
			Collection<Game_Player> players = new HashSet<Game_Player>();
			
			for (Player player : pls)
				players.add(ObjectConverter.convertPlayer(player));
			
			return players;
		}
		finally
		{
			conn.close();
		}
	}

	/**
	 * Order a fleet to move to any star-system in the game-world.
	 * 
	 * @param fleet_id The fleet in question.
	 * @param destination_star_system The ultimate destination.
	 * @param urgent If true, then order will be placed in the beginning of the queue; if false, at the end.
	 * @return True if the order is valid; false otherwise.
	 */
	public synchronized boolean moveFleet(Fleet fleet, StarSystem destination_star_system, boolean urgent) throws TPException, IOException
	{
		SequentialConnection<TP03Visitor> conn = CLIENT.getPipeline();
		
		OrderInsert order = new OrderInsert();
		order.setOtype(Orders.MOVE_ORDER); //the type of the order
		order.setId(fleet.GAME_ID); //the object at hand.
		
		if (!urgent)
			order.setSlot(-1); //sets the location of the order at the end of the queue.
		else
			order.setSlot(0); //sets the location of the order at the beginning of the queue.
		
		//setting destination:
		OrderParams.OrderParamObject destination_param = new OrderParams.OrderParamObject();
		destination_param.setObjectid(destination_star_system.GAME_ID);
		
		
		//setting the parameters:
		List<OrderParams> op = new ArrayList<OrderParams>(1);
		op.add(destination_param);
		order.setOrderparams(op, ConnectionMethods.getODbyId(order.getOtype(), conn)); 
		
		
		try
		{
			boolean result = ConnectionMethods.sendOrder(order, conn);
			return result;
		}
		finally
		{
			conn.close();
		}
	}
	
	/**
	 * Build a fleet on some planet.
	 * 
	 * @param fleet_id The fleet to be built.
	 * @param planet Where the fleet will be built.
	 * @param urgent If true, then order will be placed in the beginning of the queue; if false, at the end.
	 * @return True if the order is valid; false otherwise.
	 */
	public synchronized boolean buildFleet(Fleet fleet, Planet planet, boolean urgent) throws TPException, IOException
	{
		SequentialConnection<TP03Visitor> conn = CLIENT.getPipeline();
		
		OrderInsert order = new OrderInsert();
		order.setOtype(Orders.BUILD_FLEET); //the type of the order
		order.setId(planet.GAME_ID); //the object at hand.
		
		if (!urgent)
			order.setSlot(-1); //sets the location of the order at the end of the queue.
		else
			order.setSlot(0); //sets the location of the order at the beginning of the queue.
		
		
		//GET THE NAME OUT OF THE FLEET
		
		//GET THE SHIPS OUT OF THE FLEET
		
		
		//setting the parameters:
		List<OrderParams> op = new ArrayList<OrderParams>();
		/// REGISTER THE SHIPS TYPE
		/// REGISTER THE NAME OF THE FLEET
		
		order.setOrderparams(op, ConnectionMethods.getODbyId(order.getOtype(), conn)); 
		
		try
		{
			boolean result = ConnectionMethods.sendOrder(order, conn);
			return result;
		}
		finally
		{
			conn.close();
		}
	}
	
	

}
