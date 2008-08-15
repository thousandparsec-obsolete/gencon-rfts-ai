package gencon.robolib.RISK;

import java.io.IOException;

import net.thousandparsec.netlib.TPException;
import gencon.clientLib.RISK.ClientMethodsRISK;
import gencon.gamelib.RISK.gameobjects.Star;

public class ActionMethods 
{
	private final AdvancedMap MAP;
	private final ClientMethodsRISK CLIENT_RISK;
	
	public ActionMethods(AdvancedMap advMap, ClientMethodsRISK clientRisk)
	{
		MAP = advMap;
		CLIENT_RISK = clientRisk;
	}
	
	public boolean orderMove(Star from, Star to, int troops, boolean urgent) throws TPException, IOException
	{
		return CLIENT_RISK.orderMove(from, to, troops, urgent);
	}
	
	public boolean orderColonize(Star star, int troops, boolean urgent) throws TPException, IOException
	{
		return CLIENT_RISK.orderColonize(star, troops, urgent);
	}
	
	public boolean orderReinforce(Star star, int troops, boolean urgent) throws TPException, IOException
	{
		return CLIENT_RISK.orderReinforce(star, troops, urgent);
	}
	
	
}
