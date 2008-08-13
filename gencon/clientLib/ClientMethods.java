package gencon.clientLib;

import gencon.clientLib.RFTS.ObjectConverter;
import gencon.gamelib.Players.Game_Player;

import java.io.IOException;

import net.thousandparsec.netlib.SequentialConnection;
import net.thousandparsec.netlib.TPException;
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

}
