package gencon.gamelib.RFTS;

import gencon.gamelib.RFTS.gameobjects.Body;
import gencon.gamelib.RFTS.gameobjects.StarSystem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.Vector;

import net.thousandparsec.util.Pair;

/**
 * A representation of the game-world based on star-systems, plus some logic to help navigate.
 * 
 * @author Victor Ivri
 */
public class UniverseMapRFTS 
{
	/**
	 * The de-facto atomic unit of the {@link UniverseMapRFTS}.
	 */
	public final Collection<StarSystem> STAR_SYSTEMS;

	/**
	 * The de-jure atomic unit of the {@link UniverseMapRFTS}.
	 */
	public final Collection<Body> ALL_BODIES;
	
	
	/**
	 * The boundaries of the game-world: Pair<Pair<minX, maxX>, Pair<minY, maxY>>
	 */
	public final Pair<Pair<Long, Long>, Pair<Long, Long>> BOUNDARIES;
	
	public UniverseMapRFTS(Collection<Body> bodies)
	{
		ALL_BODIES = bodies;
		STAR_SYSTEMS = isolateStarSystems(ALL_BODIES);
		BOUNDARIES = establishBoundaries();
	}
	
	//isolates the set of star-systems out of all game-objects.
	private Collection<StarSystem> isolateStarSystems(Collection<Body> bodies)
	{
		Collection<StarSystem> stsystems = new HashSet<StarSystem>();
		for (Body bod : bodies)
			if (bod != null && bod.TYPE == Body.BodyType.STAR_SYSTEM)
				stsystems.add((StarSystem) bod);
		
		return stsystems;
	}
	
	//establishes the boundaries of the universe:
	private Pair<Pair<Long, Long>, Pair<Long, Long>> establishBoundaries()
	{
		//initializing values:
		Long maxX = Long.MIN_VALUE, maxY = Long.MIN_VALUE; 
		Long minX = Long.MAX_VALUE, minY = Long.MAX_VALUE;
		
		/*
		 * iterating and finding the boundaries:
		 * Pair<Pair<minX, maxX>, Pair<minY, maxY>>
		 */
		for (StarSystem ss : STAR_SYSTEMS)
		{
			if (ss.POSITION[0] > maxX)
				maxX = ss.POSITION[0];
			if (ss.POSITION[0] < minX)
				minX = ss.POSITION[0];
			if (ss.POSITION[1] > maxY)
				maxY = ss.POSITION[1];
			if (ss.POSITION[1] < minY)
				minY = ss.POSITION[1];
		}
		
		Pair<Long, Long> xpair = new Pair<Long, Long>(minX, maxX);
		Pair<Long, Long> ypair = new Pair<Long, Long>(minY, maxY);
		return new Pair<Pair<Long, Long>, Pair<Long, Long>>(xpair, ypair);
	}
	
//	BODY-RETREIVAL METHODS: (E.G. GET N-CLOSEST BODIES, ETC)
	
	/**
	 * Returns all the contents of that star system.
	 * 
	 * @param stsys The {@link StarSystem} in question.
	 * @return A {@link Vector} that contains what's inside the star system.
	 */
	public Collection<Body> getContents(StarSystem stsys)
	{
		return retreiveRecurs(stsys);
	}
	
	/*
	 * Helper for getContents
	 */
	private Collection<Body> retreiveRecurs(Body body) 
	{
		if (body.CHILDREN.isEmpty()) //BASE CASE
			return null;
		
		else //RECURSIVE CASE
		{
			Collection<Body> contents = new ArrayList<Body>();
			
			for (int child_id : body.CHILDREN)
			{
				//adding the child
				Body child = getById(child_id);
				contents.add(child);
				
				//adding all its contents
				Collection<Body> grand_children = retreiveRecurs(child);
				if (grand_children != null)
					for (Body g_child : grand_children)
						if (g_child != null)
							contents.add(g_child);
			}
			
			return contents;
		}
	}
	
	/**
	 * Retrieves the {@link Body} from its numeric id.
	 * 
	 * @return the {@link Body}, or null if found none.
	 */
	public Body getById(int id)
	{
		for (Body body : ALL_BODIES)
			if (body.GAME_ID == id)
				return body;
		
		//else:
		return null;
	}
	
	/**
	 * Find n closest {@link StarSystem}s to the specified {@link StarSystem} from the whole game-world, and return a {@link Vector} of them.
	 * The {@link Vector} will contain <= n {@link StarSystem}s.
	 */
	public Collection<StarSystem> getNclosestStarSystems(StarSystem ssys, int n)
	{
		return nclosest(ssys, STAR_SYSTEMS, n);
	}
	
	
	/**
	 * Find n closest {@link StarSystem}s to the specified {@link StarSystem} from some collection, and return a {@link Collection} of them.
	 * The {@link Collection} will contain <= n {@link StarSystem}s.
	 */
	public Collection<StarSystem> getNclosestStarSystems(StarSystem ssys, Collection<StarSystem> collection, int n)
	{
		return nclosest(ssys, collection, n);
	}
	
	
	private Collection<StarSystem> nclosest(StarSystem ssys, Collection<StarSystem> collection, int n)
	{
		TreeMap<Double, StarSystem> distance_to_body = new TreeMap<Double, StarSystem>();
		
		//make sure collection does not contain ssys:
		if (collection.contains(ssys))
			collection.remove(ssys);
		
		//puts all bodies in the map
		for (StarSystem s1 : collection)
			distance_to_body.put(getDistance(s1, ssys), s1);
		
		//finds n-closest, or as long as there are bodies
		Collection<StarSystem> nclosest = new HashSet<StarSystem>(n);
		
		for (byte i = 0; i < n && i < collection.size(); i++) //assumption: a star system should exist, as long as i < collection.size().
		{
			//System.out.print(distance_to_body.ceilingKey((long)0));
			double id = new Double(distance_to_body.ceilingKey(0.0)).doubleValue(); //gets the closest distance to 0.
			StarSystem s2 = distance_to_body.get(id);
			nclosest.add(s2);
			distance_to_body.remove(id); //removes the selected body from the object-distance mapping, so it won't be counted twice
		}
		
		return nclosest;
	}
	
	
	/**
	 * Calculates the distance between two {@link Body}s, a and b.
	 * Neither a or b can be null.
	 * 
	 * NOTE: Incorporates the up-down, left-right wrap-around by using the smallest x- and y- distances between bodies in the calculation.
	 */
	public double getDistance (Body a, Body b)
	{
		//CALCULATIONS TOO LARGE FOR LONG, CASTING TO DOUBLE:
		double x_dist = shortestDistance(a, b, 'x');
		double y_dist = shortestDistance(a, b, 'y');
		
		//System.out.println("::Distance:: x: " + x_dist + " y: " + y_dist);
		
		return Math.sqrt(x_dist * x_dist + y_dist * y_dist); //close enough approximation!
	}
	
	/*
	 * Returns the shortest distance between two bodies on an axis (x or y), 
	 * based on assumption of an all-around wrap-around that uses normalized space.
	 */
	private double shortestDistance(Body a, Body b, char axis)
	{
		double option1 = 0, option2 = 0, option3 = 0;
		
		if (axis == 'x')
		{
				option1 = Math.abs(a.POSITION[0] - b.POSITION[0]); //x-dist b/w bodies 
				option2 = distToBoundary(a, 'l') + distToBoundary(b, 'r'); //x-dist b/w a-->lb + rb-->b 
				option3 = distToBoundary(a, 'r') + distToBoundary(b, 'l');//x-dist b/w a-->rb + lb-->b
		}
		else if (axis == 'y')
		{
				option1 = Math.abs(a.POSITION[1] - b.POSITION[1]); //y-dist b/w bodies 
				option2 = distToBoundary(a, 'u') + distToBoundary(b, 'd'); //y-dist b/w a-->ub + db-->b 
				option3 = distToBoundary(a, 'd') + distToBoundary(b, 'u');//y-dist b/w a-->db + ub-->b 
		}
		return Math.min(option1, Math.min(option2, option3)); 
	}
	
	/*
	 * side : 'u' up, 'd' down, 'l' left, 'r' right
	 */
	private double distToBoundary(Body b, char side) 
	{
		double dist = 0;

		if (side == 'u')
			dist = Math.abs(b.POSITION[1] - BOUNDARIES.right.right);
		else if (side == 'd')
			dist = Math.abs(b.POSITION[1] - BOUNDARIES.right.left);
		else if (side == 'l')
			dist = Math.abs(b.POSITION[0] - BOUNDARIES.left.left);
		else if (side == 'r')
			dist = Math.abs(b.POSITION[0] - BOUNDARIES.left.right);
		
		return dist;
	}
}
