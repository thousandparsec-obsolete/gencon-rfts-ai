package gencon.gamelib.RISK;

import java.io.IOException;

import net.thousandparsec.netlib.TPException;
import gencon.Master;
import gencon.clientLib.RISK.ClientMethodsRISK;
import gencon.gamelib.FullGameStatus;

public class FullGameStatusRISK implements FullGameStatus
{
	private final Master MASTER;
	private final ClientMethodsRISK CLIENT_RISK;
	
	public FullGameStatusRISK(Master master)
	{
		MASTER = master;
		CLIENT_RISK = (ClientMethodsRISK) MASTER.CLIENT.getClientMethods();
	}
	
	public void init() throws IOException, TPException {
		// TODO Auto-generated method stub
		
	}
	
	public void incrementTurn() throws IOException, TPException {
		// TODO Auto-generated method stub
		
	}
	
	public boolean checkIfImAlive() {
		// TODO Auto-generated method stub
		return false;
	}



}
