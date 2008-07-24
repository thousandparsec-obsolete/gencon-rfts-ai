package gencon.gamelib;

import gencon.Master;
import gencon.clientLib.Client;
import gencon.clientLib.ClientMethodsRFTS;
import gencon.gamelib.RFTS.UniverseMapRFTS;
import gencon.gamelib.RFTS.gameobjects.Body;
import gencon.gamelib.RFTS.gameobjects.StarSystem;

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
	
	private Pair<UniverseMapRFTS, Players> currentStatus;
	private List<Pair<UniverseMapRFTS, Players>> gameHistory;
	
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
	
	public void init() throws IOException, TPException
	{
		player_name = MASTER.CLIENT.getPlayerName();
		gameHistory = new ArrayList<Pair<UniverseMapRFTS,Players>>(HISTORY_DEPTH);
		incrementTurn();
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
		UniverseMapRFTS map = makeMap();
		//retreive new list of players:
		Players pl = setPlayers(map.ALL_BODIES);
		
		//redirect reference to new status:
		currentStatus = new Pair<UniverseMapRFTS, Players>(map, pl); 
		
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

	
	private UniverseMapRFTS makeMap() throws IOException, TPException
	{
		Collection<Body> bodies = ((ClientMethodsRFTS)MASTER.CLIENT.getClientMethods()).getAllObjects();
		return new UniverseMapRFTS(bodies);
	}
	
	private Players setPlayers(Collection<Body> game_objects) throws IOException, TPException
	{
		Collection<Game_Player> newPlayers = MASTER.CLIENT.getAllPlayers(game_objects);
		return new Players(getPlayerName(), newPlayers);
	}
	

	
	
	/**  
	 * @return A deep copy of the current status.
	 */
	public Pair<UniverseMapRFTS, Players> getCurrentStatus()
	{
		if (currentStatus != null)
			return deepCopyOfStatus(currentStatus);
		else
			return null;
	}
	
	/**
	 * @return A deep copy of the game history, up to (and including) the <code>HISTORY_DEPTH</code>. Does not include the current status.
	 */
	public List<Pair<UniverseMapRFTS, Players>> getHistory()
	{
		List<Pair<UniverseMapRFTS, Players>> history = new ArrayList<Pair<UniverseMapRFTS,Players>>(gameHistory.size());
		
		for (Pair<UniverseMapRFTS, Players> status : gameHistory)
			if (status != null)
				history.add(deepCopyOfStatus(status));
		
		return history;
	}
	
	private Pair<UniverseMapRFTS, Players> deepCopyOfStatus(Pair<UniverseMapRFTS, Players> status)
	{
		return new Pair<UniverseMapRFTS, Players>
			(new UniverseMapRFTS(status.left.ALL_BODIES), 
				new Players(status.right.ME, status.right.PLAYERS));
	}
	
}
