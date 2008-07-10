package gencon.gamelib;


import java.util.Collection;
import java.util.List;
import java.util.Vector;

/**
 * A class which holds all players that are currently in the game.
 * 
 * @author Victor Ivri
 *
 */
public class Players
{
	public final Collection<Game_Player> PLAYERS;
	public final String ME;
	
	public Players(String me, Collection<Game_Player> players)
	{
		PLAYERS = players;
		ME = me;
	}
	
	/**
	 * 
	 * @param id The player's number in the game.
	 * @return A {@link Game_Player} that represents that particular player in the game.
	 */
	public Game_Player getById(int id)
	{
		for (Game_Player gp : PLAYERS)
			if (gp.NUM == id)
				return gp;
		
		return null; //if found none.
	}
	
	/**
	 * 
	 * @return A {@link Game_Player} which represents this client, or null if none found.
	 */
	public Game_Player getMe()
	{
		for (Game_Player gp : PLAYERS)
			if (gp.NAME.equals(ME))
				return gp;
		
		return null; //if found none.
	}
}
