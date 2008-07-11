package gencon.robolib;

import gencon.gamelib.UniverseMap;
import gencon.gamelib.gameobjects.Fleet;
import gencon.gamelib.gameobjects.Planet;
import gencon.gamelib.gameobjects.Ships;
import gencon.gamelib.gameobjects.StarSystem;
import gencon.robolib.AdvancedMap.Sectors.Sector;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import net.thousandparsec.netlib.TPException;

public class HigherLevelActions 
{
	final ActionMethods ACT;

	HigherLevelActions(ActionMethods actMeth) 
	{
		ACT = actMeth;
	}
	
	
	////////////////////////////////////
	///////
	///////	FLEET MAKING
	/////// 
	////////////////////////////////////
	

	
	int makeAttackFleet(Planet planet, short ships, byte techLevel)
	{
		switch (techLevel)
		{
			case (1): return ACT.createFleet(planet, new Ships(0, 0, ships, 0, 0, 0));
			case (2): return ACT.createFleet(planet, new Ships(0, 0, 0, ships, 0, 0));
			case (3): return ACT.createFleet(planet, new Ships(0, 0, 0, 0, ships, 0));
			case (4): return ACT.createFleet(planet, new Ships(0, 0, 0, 0, 0, ships));
			default: return -2; //if happens, there's a bug!
		}
	}
	
	int makeDefendedColonizeFleet(Planet planet, short colonizers, short escort, byte techLevel)
	{
		switch (techLevel)
		{
			case (1): return ACT.createFleet(planet, new Ships(colonizers, 0, escort, 0, 0, 0));
			case (2): return ACT.createFleet(planet, new Ships(colonizers, 0, 0, escort, 0, 0));
			case (3): return ACT.createFleet(planet, new Ships(colonizers, 0, 0, 0, escort, 0));
			case (4): return ACT.createFleet(planet, new Ships(colonizers, 0, 0, 0, 0, escort));
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
	///////	RESOURCE PRODUCTION:
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
		Collection<StarSystem> checkpoints = ACT.map().getNclosestStarSystems(target, checkpointsPerLoop);
		//---------
		
		//DO THE TOUR N-TIMES:
		for (int i = 0; i < loops; i++)
			ACT.smartTour(fl, checkpoints, target);
			//	note that the target star-system will be the last in each tour-round.
		
		//GO TO FINISH DESTINATION:
		ACT.moveFleet(fl, finish, false);
	}
	
	
	void scoutSectorAndStayThere(Fleet fl, Sector sec) throws IOException, TPException
	{
		Collection<Integer> ids = sec.getContents();
		Collection<StarSystem> checkpoints = new HashSet<StarSystem>();

		//the checkpoints:
		for (Integer id : ids)
			checkpoints.add((StarSystem)ACT.map().getById(id.intValue()));
		
		//extracts the target system from the array, and makes a new collection from the remainder:
		Object[] array = checkpoints.toArray();
		StarSystem target = (StarSystem) array[0];
		
		Collection<StarSystem> newCheckpoints = new HashSet<StarSystem>();
		for (int i = 1; i < array.length; i++)
			newCheckpoints.add((StarSystem) array[i]);
		
		//do the tour:
		ACT.smartTour(fl, newCheckpoints, target);
	}
	
	
}
