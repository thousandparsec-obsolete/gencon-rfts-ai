package gencon.clientLib;

import gencon.clientLib.RFTS.ObjectConverter;
import gencon.gamelib.Players.Game_Player;

import java.io.IOException;
import java.util.Collection;

import net.thousandparsec.netlib.SequentialConnection;
import net.thousandparsec.netlib.TPException;
import net.thousandparsec.netlib.tp03.Design;
import net.thousandparsec.netlib.tp03.Player;
import net.thousandparsec.netlib.tp03.TP03Visitor;


public abstract class ClientMethods 
{
	public final Client CLIENT;
	
	public ClientMethods(Client client)
	{
		CLIENT = client;
	}
	
	public synchronized Game_Player getPlayerById(int id) throws IOException, TPException
	{
		SequentialConnection<TP03Visitor> conn = CLIENT.getPipeline();
		Player pl = ConnectionMethods.getPlayerById(id, conn);
		conn.close();
		
		return ObjectConverter.convertPlayer(pl);
	}
	
	public synchronized Object getObjectById(int id) throws IOException, TPException
	{
		SequentialConnection<TP03Visitor> conn = CLIENT.getPipeline();
		try
		{
			return ConnectionMethods.getObjectById(conn, id);
		}
		finally
		{
			conn.close();
		}
	}
	
	public synchronized Collection<net.thousandparsec.netlib.tp03.Object> getAllObjects() throws IOException, TPException
	{
		SequentialConnection<TP03Visitor> conn = CLIENT.getPipeline();
		try
		{
			return ConnectionMethods.getAllObjects(conn);
		}
		finally
		{
			conn.close();
		}
	}
	
	public synchronized int getTimeRemaining() throws IOException, TPException
	{
		SequentialConnection<TP03Visitor> conn = CLIENT.getPipeline();
		
		try
		{
			return ConnectionMethods.getTimeRemaining(conn);
		}
		finally
		{
			conn.close();
		}
	}
	
	public Collection<Design> getDesigns() throws TPException, IOException
	{
		SequentialConnection<TP03Visitor> conn = CLIENT.getPipeline();
		
		try
		{
			return ConnectionMethods.getDesigns(conn);
		}
		finally
		{
			conn.close();
		}
	}

}
