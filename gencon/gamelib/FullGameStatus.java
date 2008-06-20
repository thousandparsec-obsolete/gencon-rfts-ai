package gencon.gamelib;

import gencon.clientLib.Client;
import gencon.gamelib.gameobjects.Body;
import gencon.gamelib.gameobjects.Universe;

import java.io.IOException;
import java.util.*;

import net.thousandparsec.netlib.TPException;
import net.thousandparsec.netlib.tp03.Object;
import net.thousandparsec.netlib.tp03.ObjectParams;
import net.thousandparsec.netlib.tp03.Player;
import net.thousandparsec.netlib.tp03.Object.ContainsType;
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
		//first, archive the previous status.
		gameHistory.add(getCurrentStatus());
		//remove oldest element from archive, if it exceeds specified size.
		if (gameHistory.indexOf(getCurrentStatus()) >= HISTORY_DEPTH)
			gameHistory.remove(0); 
		
		//retreive new list of players:
		Players pl = makePlayers();
		System.out.println("Players retreived.");
		//generate new map:
		UniverseMap map = makeMap(pl);
		System.out.println("Map generated.");
		
		//redirect reference to new status:
		currentStatus = new Pair<UniverseMap, Players>(map, pl); 
		
	}
	
	
	private Players makePlayers() throws IOException, TPException
	{
		Vector<Player> gotPlayers = CLIENT.getAllPlayers();
			//this line throws the exceptions!
		Vector<Game_Player> newPlayers = new Vector<Game_Player>();
		
		
		for (Player player : gotPlayers)
			if (player != null)
				newPlayers.add(new Game_Player(player.getId(), player.getName()));
		
		return new Players(PLAYER_NAME, newPlayers);
	}
	
	private UniverseMap makeMap(Players pl) throws IOException, TPException
	{
		Vector<Object> objects = CLIENT.receiveAllObjects();
		Vector<Body> bodies = new Vector<Body>(objects.size());
		
		for (Object obj : objects)
		{
			if (obj != null)
			{
				int parent = -2;
				
				if (obj.getObject().getParameterType() == ObjectParams.Universe.PARAM_TYPE)
					parent = Universe.UNIVERSE_PARENT;
				else
					parent = findParent(objects, obj).getId(); //if it's not a universe, it must have a parent! If rule broken, null pointer will be thrown/
			
					bodies.add(ObjectConverter.ConvertToBody(obj, parent, CLIENT, pl));
			}
		}
		
		return new UniverseMap(bodies);
	}
	
	/*
	 * Helper method for makeMap().
	 * Returns the immediate parent of the object
	 */
	private Object findParent(Vector<Object> objects, Object child)
	{
		for (Object obj : objects)
			if (obj != null)
				for (ContainsType ct : obj.getContains())
					if (ct.getId() == child.getId())
						return obj;	
		
		//IF NOT FOUND:
		return null;
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
			(new UniverseMap(status.left.BODIES), 
				new Players(status.right.ME, status.right.PLAYERS));
	}
	
}
