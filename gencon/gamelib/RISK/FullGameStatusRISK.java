package gencon.gamelib.RISK;

import java.io.IOException;

import net.thousandparsec.netlib.TPException;
import net.thousandparsec.util.Pair;
import gencon.Master;
import gencon.clientLib.RISK.ClientMethodsRISK;
import gencon.gamelib.FullGameStatus;
import gencon.gamelib.Players;

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
		
	}
	
	
	public void incrementTurn() throws IOException, TPException 
	{
		
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
	
	
	private Pair<UniverseMap, Players> deepCopyCurrentStatus()
	{
		return new Pair<UniverseMap, Players>(new UniverseMap(currentStatus.left), 
				new Players(currentStatus.right.ME, currentStatus.right.PLAYERS));
	}


}
