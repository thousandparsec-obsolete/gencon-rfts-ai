package gencon.robolib;

import gencon.gamelib.UniverseMap;
import gencon.gamelib.gameobjects.Fleet;
import gencon.gamelib.gameobjects.StarSystem;

import java.io.IOException;
import java.util.Vector;

import net.thousandparsec.netlib.TPException;

public class HigherLevelActions 
{
	final ActionMethods ACT;
	private UniverseMap map;
	
	public HigherLevelActions(ActionMethods actMeth) 
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
	
	/**
	 * Visits n-closest star-systems (target included) for a specified number of times.
	 * The target star-system will always be the last one on each patrol.
	 *
	 * @param target The epicentre of the patrol.
	 * @param checkpointsPerLoop The amount of closest star-systems to visit.
	 * @param loops The number of times to perform the operation.
	 * @param finish The final destination.
	 */
	void patrolAroundSystem(Fleet fl, StarSystem target, short checkpointsPerLoop, short loops, StarSystem finish) throws IOException, TPException
	{
		//the checkpoints for patrol:
		Vector<StarSystem> checkpoints = map.getNclosestStarSystems(target, checkpointsPerLoop);
		//---------
		
		//DO THE TOUR N-TIMES:
		for (int i = 0; i < loops; i++)
			ACT.tour(fl, checkpoints, target);
			//	note that the target star-system will be the last in each tour-round.
		
		//GO TO FINISH DESTINATION:
		ACT.moveFleet(fl, finish, false);
	}
}
