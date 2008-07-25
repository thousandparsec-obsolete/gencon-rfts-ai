package gencon.clientLib.RFTS;

import gencon.clientLib.Client;
import gencon.clientLib.ClientMethods;
import gencon.clientLib.ConnectionMethods;
import gencon.gamelib.AbstractGameObject;
import gencon.gamelib.Players.Game_Player;
import gencon.gamelib.RFTS.gameobjects.Body;
import gencon.gamelib.RFTS.gameobjects.Fleet;
import gencon.gamelib.RFTS.gameobjects.StarSystem;
import gencon.gamelib.RFTS.gameobjects.Universe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import net.thousandparsec.netlib.SequentialConnection;
import net.thousandparsec.netlib.TPException;
import net.thousandparsec.netlib.tp04.Object;
import net.thousandparsec.netlib.tp04.ObjectParams;
import net.thousandparsec.netlib.tp04.Player;
import net.thousandparsec.netlib.tp04.TP04Visitor;
import net.thousandparsec.netlib.tp04.Object.ContainsType;

public class ClientMethodsRFTS extends ClientMethods
{
	public ClientMethodsRFTS(Client client)
	{
		super(client);
	}

	public synchronized List<Body> getAllObjects() throws IOException, TPException
	{
		SequentialConnection<TP04Visitor> conn = CLIENT.getPipeline();
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
	
	
	public synchronized Game_Player getPlayerById(int id) throws IOException, TPException
	{
		SequentialConnection<TP04Visitor> conn = CLIENT.getPipeline();
		Player pl = ConnectionMethods.getPlayerById(id, conn);
		conn.close();
		
		return ObjectConverter.convertPlayer(pl);
	}

	
	public synchronized Collection<Game_Player> getAllPlayers(Collection<Body> game_objects) throws IOException, TPException
	{
		SequentialConnection<TP04Visitor> conn = CLIENT.getPipeline();
		
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
	 * @return The number of turns for the order to complete, or -1 if it's an illegal order.
	 */
	public synchronized boolean moveFleet(Fleet fleet, StarSystem destination_star_system, boolean urgent) throws TPException, IOException
	{
		SequentialConnection<TP04Visitor> conn = CLIENT.getPipeline();
		try
		{
			boolean result = ConnectionMethods.orderMove(fleet, destination_star_system, urgent, conn);
			return result;
		}
		finally
		{
			conn.close();
		}
	}

}
