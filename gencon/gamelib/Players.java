package gencon.gamelib;

import java.util.Vector;

import net.thousandparsec.netlib.SequentialConnection;
import net.thousandparsec.netlib.TPException;
import net.thousandparsec.netlib.tp03.GetPlayer;
import net.thousandparsec.netlib.tp03.Player;
import net.thousandparsec.netlib.tp03.Sequence;
import net.thousandparsec.netlib.tp03.TP03Visitor;
import net.thousandparsec.netlib.tp03.GetWithID.IdsType;

/**
 * A class which holds all players that are currently in the game.
 * 
 * @author Victor Ivri
 *
 */
public class Players
{
	private Vector<Game_Player> players;
	public final String ME;
	
	public Players(String me)
	{
		players = new Vector<Game_Player>();
		ME = me;
	}
	
	/**
	 * Retreives all players from the server.
	 * 
	 * @param conn A {@link SequentialConnection}. Method not responsible for closing it.
	 */
	public void retreivePlayers(SequentialConnection<TP03Visitor> conn)
	{
		Vector<Game_Player> newPlayers = new Vector<Game_Player>();
		
		GetPlayer getme = new GetPlayer();
		for (int i = 0; i < 100; i++)  //100 is really just some large number. There really should not be over 100 players in the game!!
		{
			getme.getIds().add(new IdsType(i));
		}
		try
		{
			Player player;
			Sequence seq = conn.sendFrame(getme, net.thousandparsec.netlib.tp03.Sequence.class);
			int number = seq.getNumber();
			
			for (int j = 1; j < number; j++)
			{
				try
				{
					player = conn.receiveFrame(net.thousandparsec.netlib.tp03.Player.class);
					newPlayers.add(new Game_Player(player.getId(), player.getName()));
				}
				catch (TPException ignore){}
			}
		}
		catch (Exception e)
		{
			System.out.println("unsuccessful retreiving players.");
		}
		
		players = newPlayers;
	}
	
	/**
	 * 
	 * @param id The player's number in the game.
	 * @return A {@link Game_Player} that represents that particular player in the game.
	 */
	public Game_Player getById(int id)
	{
		for (Game_Player gp : players)
			if (gp != null && gp.NUM == id)
				return gp;
		
		return null; //if found none.
	}
	
	public Game_Player getMe()
	{
		for (Game_Player gp : players)
			if (gp != null && gp.NAME == ME)
				return gp;
		
		return null; //if found none.
	}
	
	/**
	 * A class representing a player in the game.
	 */
	class Game_Player
	{
		public final int NUM;
		public final String NAME;
		
		public Game_Player(int number, String name)
		{
			NUM = number;
			NAME = name;
		}
	}
}
