package gencon.gamelib;

import java.util.*;
import net.thousandparsec.util.*;

/**
 * Stores all info about the current game, plus a history of turns up to a certain depth.
 *  
 * @author Victor Ivri
 */
public class FullGameStatus
{
	private Pair<UniverseMap, UniverseTree> currentStatus;
	private Vector<Pair<UniverseMap, UniverseTree>> gameHistory;
	private Players players;
	public final short DIFFICULTY;
	/**
	 * The depth of game-history this class holds in memory.
	 */
	public final short HISTORY_DEPTH = 10;
	
	public FullGameStatus(short difficulty, String playerName)
	{
		players = new Players(playerName);
		DIFFICULTY = difficulty;
	}
	
	/**
	 * 
	 * @param status The new status of the game-world.
	 */
	public void incrementTurn(Pair<UniverseMap, UniverseTree> status)
	{
		gameHistory.add(currentStatus);
		currentStatus = status;
	}
	
	
	/**
	 * 
	 * @return An unsafe reference to the current status of the game.
	 */
	public Pair<UniverseMap, UniverseTree> getCurrentStatus()
	{
		return currentStatus;
	}
}
