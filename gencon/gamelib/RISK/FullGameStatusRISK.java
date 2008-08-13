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
import gencon.gamelib.RISK.gameobjects.RiskGameObject;

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
		incrementTurn();
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
		
		currentStatus = new Pair<UniverseMap, Players>(map, players);
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
