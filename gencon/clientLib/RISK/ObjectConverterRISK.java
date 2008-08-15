package gencon.clientLib.RISK;


import java.util.Collection;
import java.util.HashSet;

import net.thousandparsec.netlib.tp03.Object;
import net.thousandparsec.netlib.tp03.ObjectParams;
import net.thousandparsec.netlib.tp03.Object.ContainsType;
import net.thousandparsec.netlib.tp03.ObjectParams.Planet;
import net.thousandparsec.netlib.tp03.ObjectParams.Planet.ResourcesType;
import gencon.clientLib.ObjectConverterGeneric;
import gencon.gamelib.RISK.gameobjects.Constellation;
import gencon.gamelib.RISK.gameobjects.Star;
import gencon.gamelib.RISK.gameobjects.Wormhole;

public class ObjectConverterRISK extends ObjectConverterGeneric
{
	private ObjectConverterRISK(){};
	
	public synchronized static Star convertStar(Object star_system, Object planet)
	{
		assert star_system.getObject().getParameterType() == ObjectParams.StarSystem.PARAM_TYPE
			&& planet.getObject().getParameterType() == ObjectParams.Planet.PARAM_TYPE;
		
		String name = star_system.getName();
		int starId = planet.getId();
		int owner = ((Planet)planet.getObject()).getOwner();
		int army = getArmy((Planet)planet.getObject()); 
		int reinforcements = getReinforcements((Planet)planet.getObject());
		
		Star star = new Star(name, starId);
		star.setArmy(army);
		star.setAvailableReinforcements(reinforcements);
		star.setOwner(owner);
		
		return star;
	}
	
	
	private synchronized static int getArmy(Planet planet)
	{
		ResourcesType army = planet.getResources().get(0);
		assert army.getId() == 1;
		return army.getUnits();
	}
	
	private synchronized static int getReinforcements(Planet planet)
	{
		ResourcesType army = planet.getResources().get(0);
		assert army.getId() == 1;
		return army.getUnitsminable();
	}
	
	public synchronized static Constellation convertConstellation(Object galaxy)
	{
		assert galaxy.getObject().getParameterType() == ObjectParams.Galaxy.PARAM_TYPE;
		
		String name = galaxy.getName();
		int gameId = galaxy.getId();
		
		Collection<Integer> starIds = new HashSet<Integer>();
		for (ContainsType ct : galaxy.getContains())
			starIds.add(ct.getId());
		
		return new Constellation(name, gameId, starIds);
	}
	
	public synchronized static Wormhole convertWormhole(Object wormhole)
	{
		assert wormhole.getObject().getParameterType() == ObjectParams.Wormhole.PARAM_TYPE;
		
		int startIndex = wormhole.getName().indexOf(" to ");
		int endIndex = startIndex + 4;
		
		String starA = wormhole.getName().substring(0, startIndex);
		String starB = wormhole.getName().substring(endIndex, wormhole.getName().length());
		
		//testing:
		//System.out.println("Wormhole: Converting from '" + wormhole.getName() + "' to: end a: '" + starA + "' and end b: '" + starB + "'");
		
		return new Wormhole(starA, starB);
	}
	
}
