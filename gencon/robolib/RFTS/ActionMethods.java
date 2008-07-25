package gencon.robolib.RFTS;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import net.thousandparsec.netlib.TPException;

import gencon.clientLib.Client;
import gencon.clientLib.RFTS.ClientMethodsRFTS;
import gencon.gamelib.RFTS.FullGameStatusRFTS;
import gencon.gamelib.RFTS.UniverseMapRFTS;
import gencon.gamelib.RFTS.gameobjects.Body;
import gencon.gamelib.RFTS.gameobjects.Fleet;
import gencon.gamelib.RFTS.gameobjects.Planet;
import gencon.gamelib.RFTS.gameobjects.Ships;
import gencon.gamelib.RFTS.gameobjects.StarSystem;

public class ActionMethods 
{
	private final Client CLIENT;
	public final AdvancedMap MAP;
	
	ActionMethods(Client client, FullGameStatusRFTS fgs)
	{
		CLIENT = client;
		MAP = new AdvancedMap(fgs);
	}

	/*
	 * A convenience method; eqiuivalent to MAP.genBasicMap().
	 */
	UniverseMapRFTS map()
	{
		return MAP.getBasicMap();
	}
	
	
	/**
	 * Order a fleet to move to any star-system in the game-world.
	 * 
	 * @param fleet_id The fleet in question.
	 * @param destination_star_system The ultimate destination.
	 * @param urgent If true, then order will be placed in the beginning of the queue; if false, at the end.
	 * @return Whether or not the order was successful.
	 */
	boolean moveFleet(Fleet fleet, StarSystem destination_star_system, boolean urgent) throws TPException, IOException
	{
		return ((ClientMethodsRFTS)CLIENT.getClientMethods()).moveFleet(fleet, destination_star_system, urgent);
	}
	
	
	/**
	 * Creates a fleet on a certain {@link Planet}, with specified {@link Ships}.
	 * 
	 * @return The number of turns for the order to complete, or -1 if it's an illegal order.
	 */
	int createFleet(Planet planet, Ships ships)
	{
		/// TO DO!!!

		//for now!
		return -1;
	
	}
	
	
	/**
	 * Sends a {@link Fleet} to tour a collection of {@link StarSystem}s.
	 * The fleet visits each one -once-, then goes to the finish.
	 * NOTE: THE ROUTE ISN'T FULLY OPTIMIZED, TO SAVE ON RUNTIME, BUT IT GIVES A DECENT ESTIMATE.
	 * 
	 */
	void smartTour(Fleet fleet, Collection<StarSystem> checkpoints, StarSystem finish) throws IOException, TPException
	{
		//Finding the starting-point:
		Body b = map().getById(fleet.PARENT);
		StarSystem start;
		
		if (b.TYPE == Body.BodyType.STAR_SYSTEM)
			start = (StarSystem) b;
		else
		{
			assert b.TYPE == Body.BodyType.PLANET; //the only other option!
			start = (StarSystem)map().getById(b.PARENT);
		}
		//-----------------------
		
		//get the route:
		long time1 = System.currentTimeMillis();//DEBUG
		List<StarSystem> route = findRoute(start, checkpoints, finish);
		long time2 = System.currentTimeMillis();//DEBUG
		long time = time2 - time1;//DEBUG

		
				//DEBUG
				System.out.println("Route : (took " + time + " milliseconds to plan; length: " + calculateRouteLength(route)+ ")"); //for debug!
				byte cp = 1;
				for (StarSystem ss : route)
				{
					System.out.println(cp + ")" + ss.GAME_ID + " " + ss.NAME);
					cp++;
				}
				//-----------------------
		
		//assign orders:
		for (StarSystem checkpoint : route)
			moveFleet(fleet, checkpoint, false);
	}
	
	/**
	 * Finds a decently optimized route (Much better than random!) from start to finish, through the checkpoints.
	 * 
	 * My analysis shows that for some K to restrict the search down the solution tree, where K < n, 
	 * and where n is the size of the collection to be visited, this algorithm is in the order of:
	 * O(K ^ (n-K) * (K - 1)!), which is acceptable for a small K and relatively small n.
	 * 
	 * The coefficient K is chosen automatically, s.t. it's the biggest s.t. : K ^ (n-K) * (K - 1)! <= 5e4 , which is admittedly an arbitrary number :)
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
		//note: put a new anonymous List in the 'routeSoFar' place, since there is no route so far yet!
	}
	
	/*
	 * Finds an acceptable value K for the Travelling Fleet Problem!
	 */
	private byte findDecentK(int n)
	{
		//return 1 if n is too small:
		if (n < 3)
			return 1;
		
		//finding the max-K to start with:
		byte MAX_K = 10; //An arbitrary large number for starters.
		while (MAX_K > n && MAX_K > 3) //minimum MAX_K = 3;
			MAX_K--;
		
		int ACCEPTABLE = (int)5e4; //An arbitrary acceptable number of computations.
		
		//chooses the largest Big-O result <= ACCEPTABLE
		for (byte K = MAX_K; K > 1; K--) //in this computation, codition: K > 1
		{
			double result = Math.pow(K, n - K) * factoreal((byte)(K - 1));
			if (result <= ACCEPTABLE)
			{
				//debug:
				System.out.println("K: " + K + " n: " + n + " Big-O result: " + result + " branch-outs.");
				//-----
				return K;
			}
		}
		
		//SHOULDN'T HAPPEN USUALLY: (ONLY IN CASE OF VERY BIG N)
		//in case it never drops below acceptable, at least have it at 1.
		//then, it's a simple greedy algorithm (albeit not tweaked for optimum speed).
		return 1;
	}
	
	private double factoreal(byte n)
	{
		return factoreal_recurs(n);
	}
	
	private double factoreal_recurs(byte n)
	{
		//base case:
		if (n == 1)
			return 1.0;
		//recursive case:
		else
			return ((double)n) * factoreal_recurs((byte)(n - 1));
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
			Collection<StarSystem> kClosest = map().getNclosestStarSystems(start, remaining, K);
			
			/*
			///////debug:
			System.out.println("---\n");
			for (StarSystem ss : kClosest)
				System.out.print(ss.NAME + " ");
			System.out.println("---\n");
			//////
			 */
			
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
			
			//return the best one. Bam!
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
			distance += map().getDistance(route.get(i), route.get(i + 1));
		
		return distance;
	}
	

}
