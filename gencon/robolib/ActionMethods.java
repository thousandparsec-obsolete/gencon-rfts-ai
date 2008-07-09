package gencon.robolib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import net.thousandparsec.netlib.TPException;

import gencon.clientLib.Client;
import gencon.gamelib.UniverseMap;
import gencon.gamelib.gameobjects.Body;
import gencon.gamelib.gameobjects.Fleet;
import gencon.gamelib.gameobjects.Planet;
import gencon.gamelib.gameobjects.Ships;
import gencon.gamelib.gameobjects.StarSystem;

public class ActionMethods 
{
	private final Client CLIENT;
	private UniverseMap map;
	
	ActionMethods(Client client)
	{
		CLIENT = client;
	}
	
	/**
	 * Update the map at the start of every turn!! 
	 */
	void updateMap(UniverseMap newMap)
	{
		map = newMap;
	}
	
	
	/**
	 * Order a fleet to move to any star-system in the game-world.
	 * 
	 * @param fleet_id The fleet in question.
	 * @param destination_star_system The ultimate destination.
	 * @param urgent If true, then order will be placed in the beginning of the queue; if false, at the end.
	 * @return The number of turns for the order to complete, or -1 if it's an illegal order.
	 */
	int moveFleet(Fleet fleet, StarSystem destination_star_system, boolean urgent) throws TPException, IOException
	{
		return CLIENT.moveFleet(fleet, destination_star_system, urgent);
	}
	
	
	/**
	 * Creates a fleet on a certain {@link Planet}, with specified {@link Ships}.
	 * 
	 * @return The number of turns for the order to complete, or -1 if it's an illegal order.
	 */
	int createFleet(Planet planet, Ships ships)
	{
		/// TO DO!!!

	
	}
	
	
	
	
	
	
	/**
	 * Sends a {@link Fleet} to tour a collection of {@link StarSystem}s.
	 * The fleet visits each one -once-, then goes to the finish.
	 * NOTE: THE ROUTE ISN'T FULLY OPTIMIZED, TO SAVE ON RUNTIME, BUT IT GIVES A DECENT ESTIMATE
	 * 
	 */
	void smartTour(Fleet fleet, Collection<StarSystem> checkpoints, StarSystem finish) throws IOException, TPException
	{
		//Finding the starting-point:
		Body b = map.getById(fleet.PARENT);
		StarSystem start;
		
		if (b.TYPE == Body.BodyType.STAR_SYSTEM)
			start = (StarSystem) b;
		else
		{
			assert b.TYPE == Body.BodyType.PLANET; //the only other option!
			start = (StarSystem)map.getById(b.PARENT);
		}
		//-----------------------
		
		//get the route:
		List<StarSystem> route = findRoute(start, checkpoints, finish);
		
		//assign orders:
		for (StarSystem checkpoint : route)
			moveFleet(fleet, checkpoint, false);
	}
	
	/**
	 * Finds a decently optimized route (Much better than random!) from start to finish, through the checkpoints.
	 * 
	 * My analysis shows that for some K to restrict the search down the solution tree, where K < n, 
	 * and where n is the size of the collection to be visited, this algorithm is in the order of:
	 * O(K ^ (n-K) * (K - 1)!), which is acceptable for relatively small K and small n.
	 * 
	 * The coefficient K is chosen automatically, s.t. it's the biggest s.t. : K ^ (n-K) <= 2 * 10e4 , which is admittedly an arbitrary number :)
	 * 
	 * 
	 * @param start The starting point.
	 * @param checkpoints The collection of {@link StarSystem}s, the fleet must traverse.
	 * @param finish The ultimate endpoint of the journey.
	 * @return A decently optimized route (Much better than random!) from start to finish, through the checkpoints.
	 */
	List<StarSystem> findRoute(StarSystem start, Collection<StarSystem> checkpoints, StarSystem finish)
	{
		byte K = findDecentK(checkpoints.size());
		return findRouteRecurs(start, new ArrayList<StarSystem>(), checkpoints, finish, K);
//		note: put a new anonymous List in the 'routeSoFar' place, since there is no route so far yet!
	}
	
	/*
	 * Finds an acceptable value K for the Travelling Fleet Problem!
	 */
	private byte findDecentK(int n)
	{
		byte K = 0;
		
		byte MAX_K = 7; //An arbitrary large coefficient to start with
		int ACCEPTABLE = (int)(2 * 10e4); //An arbitrary acceptable number of computations
		
		for (byte i = MAX_K; i >= 1; i--)
		{
			long result = i ^ (n - i);
			if (result <= ACCEPTABLE)
			{
				K = i;
				return K;
			}
		}
		
		//SHOULDN'T HAPPEN USUALLY:
		//in case it never drops below acceptable, at least have it at 1.
		return K;
		
	}
	
	
	private List<StarSystem> findRouteRecurs(StarSystem start, List<StarSystem> routeSoFar, Collection<StarSystem> remaining, StarSystem finish, byte K)
	{
		if (remaining.isEmpty()) //BASE CASE: NO MORE CHECKPOINTS REMAINING. APPENDING THE FINAL DESTINATION TO IT:
		{
			routeSoFar.add(finish);
			return routeSoFar;
		}
		
		else //RECURSIVE CASE: SOME STAR-SYSTEMS LEFT IN THE COLLECTION.
		{
			//find k-closest:
			Collection<StarSystem> kClosest = map.getNclosestStarSystems(start, remaining, K);
			
			List<StarSystem> bestRoute = null; 
			double shortestRouteDistance = Long.MAX_VALUE;
			
			for (StarSystem ss : kClosest)
			{
				//modifying values for each tree-branch:
				
				//adding the star-system to the route:
				List<StarSystem> newRouteSoFar = new ArrayList<StarSystem>(routeSoFar);
				newRouteSoFar.add(ss);
				//removing it from the 'remaining' collection:
				List<StarSystem> newRemaining = new ArrayList<StarSystem>(remaining);
				newRemaining.remove(ss);
				
				//propogate downwards, with the star-system as the starting point:
				List<StarSystem> theRoute = findRouteRecurs(ss, newRouteSoFar, newRemaining, finish, K);
				
				//calculate the route distance:
				Double routeDistance = new Double(calculateRouteLength(theRoute));
				
				//seeing if it's the best one so far!
				if (routeDistance <= shortestRouteDistance)
				{
					bestRoute = theRoute;
					shortestRouteDistance = routeDistance;
				}
			}
			
			//return the best one!!! Bam!
			return bestRoute;
		}
	
		
	}
	
	/**
	 * Calculates the actual distance of a route.
	 */
	double calculateRouteLength(List<StarSystem> route)
	{
		double distance = 0.0;
		
		for (int i = 0; i < route.size() - 1; i++)
			distance += map.getDistance(route.get(i), route.get(i + 1));
		
		return distance;
	}
	

}
