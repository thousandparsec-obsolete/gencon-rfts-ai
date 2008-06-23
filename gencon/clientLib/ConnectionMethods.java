package gencon.clientLib;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Vector;

import net.thousandparsec.netlib.Frame;
import net.thousandparsec.netlib.SequentialConnection;
import net.thousandparsec.netlib.TPException;
import net.thousandparsec.netlib.tp03.GetObjectIDs;
import net.thousandparsec.netlib.tp03.GetObjectsByID;
import net.thousandparsec.netlib.tp03.GetPlayer;
import net.thousandparsec.netlib.tp03.GetTimeRemaining;
import net.thousandparsec.netlib.tp03.Object;
import net.thousandparsec.netlib.tp03.ObjectIDs;
import net.thousandparsec.netlib.tp03.ObjectParams;
import net.thousandparsec.netlib.tp03.Order;
import net.thousandparsec.netlib.tp03.Player;
import net.thousandparsec.netlib.tp03.Sequence;
import net.thousandparsec.netlib.tp03.TP03Visitor;
import net.thousandparsec.netlib.tp03.TimeRemaining;
import net.thousandparsec.netlib.tp03.GetWithID.IdsType;
import net.thousandparsec.netlib.tp03.IDSequence.ModtimesType;
import net.thousandparsec.netlib.tp03.ObjectParams.Fleet;
import net.thousandparsec.netlib.tp03.ObjectParams.Planet;

import gencon.utils.*;

public class ConnectionMethods 
{
	private ConnectionMethods(){}	//dummy constructor: static class.
	private final static PrintStream stout = System.out;
	
	public synchronized static Object getUniverse(SequentialConnection<TP03Visitor> conn) throws IOException, TPException
	{
		GetObjectsByID get = new GetObjectsByID();
		get.getIds().add(new IdsType(0));
		
		conn.receiveFrame(Sequence.class);
		Object universe = conn.receiveFrame(Object.class);
		//making sure this indeed is the universe:
		assert universe.getObject().getParameterType() == ObjectParams.Universe.PARAM_TYPE;
		return universe;
	}
	
	public synchronized static int getTimeRemaining(SequentialConnection<TP03Visitor> conn) throws IOException, TPException
	{
		TimeRemaining tr = null;
		tr = conn.sendFrame(new GetTimeRemaining(), net.thousandparsec.netlib.tp03.TimeRemaining.class);
		return tr.getTime();
	}
	
	public synchronized static Player getPlayerById(int id, SequentialConnection<TP03Visitor> conn) throws IOException, TPException
	{
		GetPlayer get = new GetPlayer();
		get.getIds().add(new IdsType(id));
		return conn.sendFrame(get, Player.class);
	}
	
	public synchronized static Vector<Player> getAllPlayers(SequentialConnection<TP03Visitor> conn) throws IOException, TPException
	{
		//FIRST: RETREIVE ALL OBJECTS, AND CHECK ALL AVAILABLE PLAYER-IDS:
		//A redundancy, but works fast enough, plus it avoids unnecessary clutter in code. 
		Vector<Object> objects = receiveAllObjects(conn); 
		
		Vector<Integer> playerIds = new Vector<Integer>(objects.size());
		
		//make sure player ids don't repeat:
		boolean[] flags = new boolean[objects.size()]; 
		for (int i = 0; i < flags.length; i++)
			flags[i] = false; //initializing to false. may be redundant, but just a safeguard against future changes in standards.
		
		
		final int NEUTRAL = -1; //the standard demarcation of neutral objects.
		
		//add to list of ids if object is a fleet or non-neutral planet.
		for (Object obj : objects)
		{
			if (obj != null)
			{
				if (obj.getOtype() == ObjectParams.Fleet.PARAM_TYPE)
				{
					int owner = ((Fleet)obj.getObject()).getOwner();
					if (flags[owner] == false)
					{
						playerIds.add(new Integer(owner));
						flags[owner] = true;
					}
				}
				else if (obj.getOtype() == ObjectParams.Planet.PARAM_TYPE)
				{
					int owner = ((Planet)obj.getObject()).getOwner();
					if (owner != NEUTRAL && flags[owner] == false)
					{
						playerIds.add(new Integer(owner));
						flags[owner] = true;
					}
				}
			}
		}
		
		
		//THEN, RETREIVE ALL PLAYER FRAMES, BASED ON THAT LIST:
		Vector<Player> players = new Vector<Player>();
		for (Integer id : playerIds)
			if (id != null)
			{
				players.add(getPlayerById(id, conn));
				stout.print(".");
			}

		return players;
	}
	
	private synchronized static boolean checkIfOnList(List<Integer> list, int num)
	{
		for (Integer i : list)
			if (i != null && i.intValue() == num)
				return true;
		
		//if not found:
		return false;
	}
	
	
	
	public synchronized static Vector<Object> receiveAllObjects(SequentialConnection<TP03Visitor> conn) throws IOException, TPException
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
			if (o.getOtype() == ObjectParams.Planet.PARAM_TYPE)
				stout.println("--> " + o.toString());
			objects.add(o);
		}
		
		return objects;
	}
	
	public synchronized static boolean sendOrder(int objectId, int orderType, int locationInQueue, SequentialConnection<TP03Visitor> conn) throws IOException, TPException
	{
		// TO DO !!!!!
		
	}
}
