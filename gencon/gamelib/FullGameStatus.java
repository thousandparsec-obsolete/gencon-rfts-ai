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
	public final short DIFFICULTY;
	
	private Pair<UniverseMap, Players> currentStatus;
	private Vector<Pair<UniverseMap, Players>> gameHistory;
	
	/**
	 * The depth of history that this class houses.
	 */
	public final byte HISTORY_DEPTH = 5; 
	
	public FullGameStatus(Client client, short difficulty, String playerName)
	{
		CLIENT = client;
		PLAYER_NAME = playerName;
		DIFFICULTY = difficulty;
		gameHistory = new Vector<Pair<UniverseMap,Players>>(HISTORY_DEPTH);
	}
	
	/**
	 * Requests new info from the client, and archives the old.
	 */
	public void incrementTurn() throws IOException, TPException
	{
//////	TESTING:::
		CLIENT.getResourceDescs();
		/*
		UniverseMap um = currentStatus.left;
		
		List<StarSystem> sss = um.STAR_SYSTEMS;
		
		for (StarSystem ss : sss)
			if (ss != null)
			{
				System.out.println("Star system: " + ss.NAME + " : " + ss.GAME_ID);
				List<Body> contents = um.getContents(ss);
				for (Body bod: contents)
					System.out.println("--> " + bod.NAME + " : " + bod.GAME_ID);
			}
		*/
///////////////
		
		
		//first, archive the previous status.
		gameHistory.add(getCurrentStatus());
		//remove oldest element from archive, if it exceeds specified size.
		if (gameHistory.indexOf(getCurrentStatus()) >= HISTORY_DEPTH)
			gameHistory.remove(0); 
		
		//retreive new list of players:
		Players pl = getPlayers();
		System.out.println("Players retreived.");
		//generate new map:
		UniverseMap map = makeMap(pl);
		System.out.println("Map generated.");
		
		//redirect reference to new status:
		currentStatus = new Pair<UniverseMap, Players>(map, pl); 
		
		
		
		
	}
	
	private Players getPlayers() throws IOException, TPException
	{
		Vector<Game_Player> newPlayers = CLIENT.getAllPlayers();
		return new Players(PLAYER_NAME, newPlayers);
	}
	
	private UniverseMap makeMap(Players pl) throws IOException, TPException
	{
		Vector<Body> bodies = CLIENT.receiveAllObjects();
		return new UniverseMap(bodies);
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
	 * @return A deep copy of the game history, up to (and including) the <code>HISTORY_DEPTH</code>.
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
