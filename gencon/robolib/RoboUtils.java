package gencon.robolib;

import gencon.gamelib.UniverseMap;
import gencon.gamelib.gameobjects.Body;
import gencon.gamelib.gameobjects.Fleet;
import gencon.gamelib.gameobjects.Planet;

import java.util.HashSet;
import java.util.Set;

public class RoboUtils 
{
	private final Robot AI;
	
	RoboUtils(Robot ai)
	{
		AI = ai;
	}
	

	Set<Planet> getMyPlanets()
	{
		UniverseMap um = AI.MASTER.GAME_STATUS.getCurrentStatus().left;
		int myNum = AI.MASTER.GAME_STATUS.getCurrentStatus().right.getMe().NUM;
		
		Set<Planet> myplanets = new HashSet<Planet>();
		for (Body b : um.ALL_BODIES)
			if (b.TYPE == Body.BodyType.PLANET)
			{
				Planet p = (Planet)b;
				if (p.OWNER == myNum)
					myplanets.add(p);
			}
		
		return myplanets;
	}
	
	Set<Fleet> getMyFleets()
	{
		UniverseMap um = AI.MASTER.GAME_STATUS.getCurrentStatus().left;
		int myNum = AI.MASTER.GAME_STATUS.getCurrentStatus().right.getMe().NUM;
		
		Set<Fleet> myfleet = new HashSet<Fleet>();
		for (Body b : um.ALL_BODIES)
			if (b.TYPE == Body.BodyType.FLEET)
			{
				Fleet f = (Fleet)b;
				if (f.OWNER == myNum)
					myfleet.add(f);
			}
		
		return myfleet;
	}
	
	
	
	
	
	
}
