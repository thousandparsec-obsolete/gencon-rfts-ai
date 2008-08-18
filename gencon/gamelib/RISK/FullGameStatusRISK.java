package gencon.gamelib.RISK;

import java.io.IOException;
import java.util.Collection;

import net.thousandparsec.netlib.TPException;
import net.thousandparsec.netlib.tp03.Object;
import net.thousandparsec.util.Pair;
import gencon.Master;
import gencon.clientLib.RISK.ClientMethodsRISK;
import gencon.gamelib.FullGameStatus;
import gencon.gamelib.Players;
import gencon.gamelib.Players.Game_Player;
import gencon.gamelib.RISK.gameobjects.Constellation;
import gencon.gamelib.RISK.gameobjects.RiskGameObject;
import gencon.gamelib.RISK.gameobjects.Star;

public class FullGameStatusRISK implements FullGameStatus
{
	private final Master MASTER;
	private final ClientMethodsRISK CLIENT_RISK;
	
	private Pair<UniverseMap, Players> currentStatus;
	
	public FullGameStatusRISK(Master master)
	{
		MASTER = master;
		CLIENT_RISK = (ClientMethodsRISK) MASTER.CLIENT.getClientMethods();
	}
	
	public void init() throws IOException, TPException
	{
		//don't need it (yet).
	}
	
	
	public void incrementTurn() throws IOException, TPException 
	{
		/*
		 * This method simply creates a whole whackload of new objects each turn, 
		 * although it's possible to download only the necessary data.
		 * Pending improvement!
		 */
		
		//creating map
		Collection<Object> gameObjects = CLIENT_RISK.getAllObjects();
		Collection<RiskGameObject> riskObjects = CLIENT_RISK.convertAllObjects(gameObjects);
		UniverseMap map = new UniverseMap(riskObjects);
		
		//creating players:
		Collection<Game_Player> gplyers = CLIENT_RISK.getAllPlayers(gameObjects);
		Players players = new Players(MASTER.getMyUsername(), gplyers);
		
		//initializing reinforcements:
		map.initReinforcements(players.getMe().NUM);
		
		currentStatus = new Pair<UniverseMap, Players>(map, players);
		
		/*
		//----------------------------\\
		//		TESTING :
		
		MASTER.pl("\nTESTING RISK GAME-WORLD REPRESENTATION:");
		
		for (Star s : currentStatus.left.getStars())
		{
			MASTER.pl("Star : " + s.NAME + "--" + s.GAME_ID + " Owner: " + s.getOwner() + " Army: " + s.getArmy() + " Reinforcements: " + s.getReinforcementsAvailable() + "\n  Adjacencies: ");
			for (Integer i : s.getAdjacencies())
				MASTER.pr(i.intValue() + "  ");
			
			MASTER.pl("");
		}
		
		for (Constellation c : currentStatus.left.getConstellations())
		{
			MASTER.pl("Constellation: " + c.NAME + "--" + c.GAME_ID + "\n  Stars:");
			for (Integer i : c.getStars())
				MASTER.pr(i.intValue() + "  ");
			MASTER.pl("");
		}
		*/
	}
	
	public boolean checkIfImAlive() 
	{
		return !(currentStatus.right.getMe() == null);
	}

	public FullGameStatus copyStatus() 
	{
		return new FullGameStatusRISK(this);
	}

	private FullGameStatusRISK(FullGameStatusRISK other)
	{
		CLIENT_RISK = other.CLIENT_RISK;
		MASTER = other.MASTER;
		currentStatus = other.deepCopyCurrentStatus();
	}
	
	
	public Pair<UniverseMap, Players> getCurrentStatus()
	{
		return deepCopyCurrentStatus();
	}
	
	private Pair<UniverseMap, Players> deepCopyCurrentStatus()
	{
		return new Pair<UniverseMap, Players>(new UniverseMap(currentStatus.left), 
				new Players(currentStatus.right.ME, currentStatus.right.PLAYERS));
	}
	

}
