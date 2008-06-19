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
		conn.receiveFrame(Sequence.class);
		return conn.receiveFrame(Player.class);
	}
	
	public synchronized static Vector<Player> getAllPlayers(SequentialConnection<TP03Visitor> conn) throws IOException, TPException
	{
		GetPlayer getplayers = new GetPlayer();
		for (int i = 1; i < 1000; i++)
		{
			getplayers.getIds().add(new IdsType(i));
		}

		Sequence seq = conn.sendFrame(getplayers, net.thousandparsec.netlib.tp03.Sequence.class);
		Vector<Player> players = new Vector<Player>(seq.getNumber());
		for (int j = 0; j < seq.getNumber(); j++)
		{
			try
			{
				players.add(conn.receiveFrame(net.thousandparsec.netlib.tp03.Player.class));
			}
			catch (TPException ignore){}
		}
		
		return players;
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
			objects.add(conn.receiveFrame(Object.class));
		}
		
		return objects;
	}
	
	public synchronized static void /* or boolean? */ sendOrder(/*...*/)
	{
		
	}
}
