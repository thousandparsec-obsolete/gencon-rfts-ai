package gencon.gamelib;

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
	
	public FullGameStatus(Client client, String playerName)
	{
		CLIENT = client;
		PLAYER_NAME = playerName;
		gameHistory = new Vector<Pair<UniverseMap,Players>>(HISTORY_DEPTH);
	}
	
	/**
	 * Requests new info from the client, and archives the old.
	 */
	public void incrementTurn() throws IOException, TPException
	{
//////	SOME TESTING:::
//		CLIENT.getResourceDescs();
//		CLIENT.getOrdersDesc();
//		CLIENT.getOrdersForMyObjects();
		/////////////////////////////////////
		
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
		
		
		
		/////SOME MORE TESTING:
		UniverseMap um = currentStatus.left;
		System.out.println("Getting 7 closest Bodies for each Star System:");
		
		Vector<StarSystem> sss = um.STAR_SYSTEMS;
		for (StarSystem ss : sss)
			um.getNclosestBodies(ss, 7);
		
		////////////////////////
		
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
