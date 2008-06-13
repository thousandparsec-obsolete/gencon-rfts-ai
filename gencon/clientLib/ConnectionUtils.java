package gencon.clientLib;

import java.io.PrintStream;
import java.util.Vector;

import net.thousandparsec.netlib.SequentialConnection;
import net.thousandparsec.netlib.TPException;
import net.thousandparsec.netlib.tp03.GetObjectsByID;
import net.thousandparsec.netlib.tp03.GetPlayer;
import net.thousandparsec.netlib.tp03.GetTimeRemaining;
import net.thousandparsec.netlib.tp03.Object;
import net.thousandparsec.netlib.tp03.ObjectParams;
import net.thousandparsec.netlib.tp03.Player;
import net.thousandparsec.netlib.tp03.Sequence;
import net.thousandparsec.netlib.tp03.TP03Visitor;
import net.thousandparsec.netlib.tp03.TimeRemaining;
import net.thousandparsec.netlib.tp03.GetWithID.IdsType;

import gencon.utils.*;

public class ConnectionUtils 
{
	private ConnectionUtils(){}	//dummy constructor: static class.
	private final static PrintStream stout = System.out;
	
	public synchronized static Object getUniverse(SequentialConnection<TP03Visitor> conn, Client client) throws Exception
	{
		GetObjectsByID get = new GetObjectsByID();
		get.getIds().add(new IdsType(0));
		
		conn.receiveFrame(Sequence.class);
		Object universe = conn.receiveFrame(Object.class);
		//making sure this indeed is the universe:
		assert universe.getObject().getParameterType() == ObjectParams.Universe.PARAM_TYPE;
		return universe;
	}
	
	public synchronized static int getTimeRemaining(SequentialConnection<TP03Visitor> conn) throws Exception
	{
		TimeRemaining tr = null;
		tr = conn.sendFrame(new GetTimeRemaining(), net.thousandparsec.netlib.tp03.TimeRemaining.class);
		return tr.getTime();
	}
	
	public synchronized static Player getPlayerById(int id, SequentialConnection<TP03Visitor> conn) throws Exception
	{
		GetPlayer get = new GetPlayer();
		get.getIds().add(new IdsType(id));
		conn.receiveFrame(Sequence.class);
		return conn.receiveFrame(Player.class);
	}
	
	public synchronized static Vector<Player> getAllPlayers(SequentialConnection<TP03Visitor> conn) throws Exception
	{
		GetPlayer getplayers = new GetPlayer();
		for (int i = 1; i < 100; i++)
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
}
