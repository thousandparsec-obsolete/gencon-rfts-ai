package gencon.robolib;

import gencon.gamelib.UniverseMap;
import gencon.gamelib.gameobjects.Fleet;
import gencon.gamelib.gameobjects.Planet;
import gencon.gamelib.gameobjects.Ships;
import gencon.gamelib.gameobjects.StarSystem;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import net.thousandparsec.netlib.TPException;

public class HigherLevelActions 
{
	final ActionMethods ACT;
	private UniverseMap map;

	private short strong_fleet;
	private short weak_fleet;
	
	HigherLevelActions(ActionMethods actMeth) 
	{
		ACT = actMeth;
	}
	
	/**
	 * Update the map at the start of every turn!! 
	 */
	void updateMap(UniverseMap newMap)
	{
		map = newMap;
		ACT.updateMap(newMap);
	}
	
	
	
	////////////////////////////////////
	///////
	///////	FLEET MAKING
	/////// 
	/////// (Still in very prototypical form... basic heuristics should be replaced)
	////////////////////////////////////
	
	
	short getStrongFleet()
	{
		return strong_fleet;
	}
	
	void setStrongFleet(short strength)
	{
		strong_fleet = strength;
	}
	
	short getWeakFleet()
	{
		return weak_fleet;
	}
	
	void setWeakFleet(short strength)
	{
		weak_fleet = strength;
	}
	
	
	int makeStrongAttackFleet(Planet planet, byte techLevel)
	{
		switch (techLevel)
		{
			case (1): return ACT.createFleet(planet, new Ships(0, 0, strong_fleet, 0, 0, 0));
			case (2): return ACT.createFleet(planet, new Ships(0, 0, 0, strong_fleet, 0, 0));
			case (3): return ACT.createFleet(planet, new Ships(0, 0, 0, 0, strong_fleet, 0));
			case (4): return ACT.createFleet(planet, new Ships(0, 0, 0, 0, 0, strong_fleet));
			default: return -2; //if happens, there's a bug!
		}
	}
	
	int makeWeakAttackFleet(Planet planet, byte techLevel)
	{
		switch (techLevel)
		{
			case (1): return ACT.createFleet(planet, new Ships(0, 0, weak_fleet, 0, 0, 0));
			case (2): return ACT.createFleet(planet, new Ships(0, 0, 0, weak_fleet, 0, 0));
			case (3): return ACT.createFleet(planet, new Ships(0, 0, 0, 0, weak_fleet, 0));
			case (4): return ACT.createFleet(planet, new Ships(0, 0, 0, 0, 0, weak_fleet));
			default: return -2; //if happens, there's a bug!
		}
	}
	
	int makeDefendedColonizeFleet(Planet planet, short colonizers, byte techLevel)
	{
		switch (techLevel)
		{
			case (1): return ACT.createFleet(planet, new Ships(colonizers, 0, weak_fleet, 0, 0, 0));
			case (2): return ACT.createFleet(planet, new Ships(colonizers, 0, 0, weak_fleet, 0, 0));
			case (3): return ACT.createFleet(planet, new Ships(colonizers, 0, 0, 0, weak_fleet, 0));
			case (4): return ACT.createFleet(planet, new Ships(colonizers, 0, 0, 0, 0, weak_fleet));
			default: return -2; //if happens, there's a bug!
		}
	}
	
	int makeUndefendedColonizeFleet(Planet planet, short colonizers)
	{
		return ACT.createFleet(planet, new Ships(colonizers, 0, 0, 0, 0, 0));
	}
	
	int makeScoutFleet(Planet planet)
	{
		return ACT.createFleet(planet, new Ships(0, 1, 0, 0, 0, 0));
	}
	
	
	////////////////////////////////////
	///////
	///////	RESOURCE MAKING
	///////
	////////////////////////////////////
	
	
	
	
	
	
	
	
	
	////////////////////////////////////
	///////
	///////	MOVEMENT AND MANEUVER
	///////
	////////////////////////////////////
	
	
	
	/**
	 * Visits n-closest star-systems (target included) for a specified number of times.
	 * The target star-system will always be the last one on each patrol.
	 * After the specified number of loops, the fleet will head to finish.
	 *
	 * @param target The epicentre of the patrol.
	 * @param checkpointsPerLoop The amount of closest star-systems to visit.
	 * @param loops The number of times to perform the operation.
	 * @param finish The final destination.
	 */
	void patrolAroundSystem(Fleet fl, StarSystem target, short checkpointsPerLoop, short loops, StarSystem finish) throws IOException, TPException
	{
		//the checkpoints for patrol:
		Collection<StarSystem> checkpoints = map.getNclosestStarSystems(target, checkpointsPerLoop);
		//---------
		
		//DO THE TOUR N-TIMES:
		for (int i = 0; i < loops; i++)
			ACT.smartTour(fl, checkpoints, target);
			//	note that the target star-system will be the last in each tour-round.
		
		//GO TO FINISH DESTINATION:
		ACT.moveFleet(fl, finish, false);
	}
	
	
	
	
}
