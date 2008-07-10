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
	private final Master MASTER;
	
	
	private String player_name;
	
	private Pair<UniverseMap, Players> currentStatus;
	private List<Pair<UniverseMap, Players>> gameHistory;
	
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
	public FullGameStatus(Master master)
	{
		MASTER = master;
	}
	
	public void init()
	{
		player_name = MASTER.CLIENT.getPlayerName();
		gameHistory = new ArrayList<Pair<UniverseMap,Players>>(HISTORY_DEPTH);
	}
	
	public String getPlayerName()
	{
		return new String(player_name);
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
		//retreive new list of players:
		Players pl = setPlayers(map.ALL_BODIES);
		
		//redirect reference to new status:
		currentStatus = new Pair<UniverseMap, Players>(map, pl); 
		
		MASTER.pl("Done retrieving info from server.");
		
		//SOME TESTING:
		MASTER.CLIENT.testMethods();
		/*
		//SOME MORE TESTING:
		System.out.println("Testing boundaries:");
		Pair<Pair<Long, Long>, Pair<Long, Long>> bnd = getCurrentStatus().left.BOUNDARIES;
		System.out.println("Max x: " + bnd.left.right + " Min x: " + bnd.left.left + "  Max y: " + bnd.right.right + " Min y: " + bnd.right.left);
		
		System.out.println("Star systems: " + getCurrentStatus().left.STAR_SYSTEMS.size());
		
		System.out.println("Testing for n-closest: (n = 5)");
		for (StarSystem ss : getCurrentStatus().left.STAR_SYSTEMS)
		{
			int n = 5;
			Collection<StarSystem> nclosest = getCurrentStatus().left.getNclosestStarSystems(ss, n);
			System.out.println("For star system: " + ss.GAME_ID + " : " + ss.NAME);
			for (StarSystem sts : nclosest)
				System.out.println("--> " + sts.GAME_ID + " : " + sts.NAME + " Distance: " + getCurrentStatus().left.getDistance(sts, ss));
		}
		*/
	}

	
	private UniverseMap makeMap() throws IOException, TPException
	{
		Collection<Body> bodies = MASTER.CLIENT.getAllObjects();
		return new UniverseMap(bodies);
	}
	
	private Players setPlayers(Collection<Body> game_objects) throws IOException, TPException
	{
		Collection<Game_Player> newPlayers = MASTER.CLIENT.getAllPlayers(game_objects);
		return new Players(getPlayerName(), newPlayers);
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
	public List<Pair<UniverseMap, Players>> getHistory()
	{
		List<Pair<UniverseMap, Players>> history = new ArrayList<Pair<UniverseMap,Players>>(gameHistory.size());
		
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
