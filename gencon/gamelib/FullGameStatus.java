package gencon.gamelib;

import gencon.Master;
import gencon.clientLib.Client;
import gencon.gamelib.gameobjects.Body;
import gencon.gamelib.gameobjects.StarSystem;

import java.io.IOException;
import java.util.*;

import net.thousandparsec.netlib.TPException;
import net.thousandparsec.util.*;

/**
 * Stores all info about the current game, plus a history of turns up to a certain depth.
 *  
 * @author Victor Ivri
 */
public class FullGameStatus
{
	private final Client CLIENT;
	
	public final String PLAYER_NAME;
	
	private Pair<UniverseMap, Players> currentStatus;
	private Vector<Pair<UniverseMap, Players>> gameHistory;
	
	/**
	 * The depth of history that this class houses.
	 */
	public final byte HISTORY_DEPTH = 5; 
	
	/**
	 * The only constructor for {@link FullGameStatus}.
	 * 
	 * @param client The {@link Client} that the {@link Master} class has.
	 * @param playerName The name of the player represented by this client.
	 */
	public FullGameStatus(Client client, String playerName)
	{
		CLIENT = client;
		PLAYER_NAME = playerName;
		gameHistory = new Vector<Pair<UniverseMap,Players>>(HISTORY_DEPTH);
	}
	
	/**
	 * Gets new info about the game, and archives the old, up to a set number (HISTORY_DEPTH).
	 */
	public void incrementTurn() throws IOException, TPException
	{
		//first, archive the previous status.
		gameHistory.add(getCurrentStatus());
		//remove oldest element from archive, if it exceeds specified size.
		if (gameHistory.indexOf(getCurrentStatus()) >= HISTORY_DEPTH)
			gameHistory.remove(0); 
		

		//generate new map:
		UniverseMap map = makeMap();
		System.out.println("Map generated.");
		//retreive new list of players:
		Players pl = setPlayers(map.ALL_BODIES);
		System.out.println("Players retreived.");
		
		//redirect reference to new status:
		currentStatus = new Pair<UniverseMap, Players>(map, pl); 
		
		//SOME TESTING:
	    //CLIENT.getOrdersDesc();
		CLIENT.testMove();
		
	}

	
	private UniverseMap makeMap() throws IOException, TPException
	{
		Vector<Body> bodies = CLIENT.getAllObjects();
		return new UniverseMap(bodies);
	}
	
	private Players setPlayers(List<Body> game_objects) throws IOException, TPException
	{
		Vector<Game_Player> newPlayers = CLIENT.getAllPlayers(game_objects);
		return new Players(PLAYER_NAME, newPlayers);
	}
	

	
	
	/**  
	 * @return A deep copy of the current status.
	 */
	public Pair<UniverseMap, Players> getCurrentStatus()
	{
		if (currentStatus != null)
			return deepCopyOfStatus(currentStatus);
		else
			return null;
	}
	
	/**
	 * @return A deep copy of the game history, up to (and including) the <code>HISTORY_DEPTH</code>. Does not include the current status.
	 */
	public Vector<Pair<UniverseMap, Players>> getHistory()
	{
		Vector<Pair<UniverseMap, Players>> history = new Vector<Pair<UniverseMap,Players>>(gameHistory.size());
		
		for (Pair<UniverseMap, Players> status : gameHistory)
			if (status != null)
				history.add(deepCopyOfStatus(status));
		
		return history;
	}
	
	private Pair<UniverseMap, Players> deepCopyOfStatus(Pair<UniverseMap, Players> status)
	{
		return new Pair<UniverseMap, Players>
			(new UniverseMap(status.left.ALL_BODIES), 
				new Players(status.right.ME, status.right.PLAYERS));
	}
	
}
