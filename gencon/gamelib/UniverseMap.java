package gencon.gamelib;

import gencon.gamelib.gameobjects.Body;
import gencon.gamelib.gameobjects.Planet;
import gencon.gamelib.gameobjects.StarSystem;
import gencon.gamelib.gameobjects.Universe;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import net.thousandparsec.util.Pair;

/**
 * A representation of the game-world based on star-systems, plus some logic to help navigate.
 * 
 * @author Victor Ivri
 */
public class UniverseMap 
{
	/**
	 * The de-jure atomic unit of the {@link UniverseMap}.
	 */
	public final Vector<StarSystem> STAR_SYSTEMS;
	
	/**
	 * The de-facto atomic unit of the {@link UniverseMap}.
	 */
	public final Vector<Body> ALL_BODIES;
	
	/**
	 * The dimensions of the universe, in the following format:
	 * Pair<sizeUnitsSquare, Pair<centerX, centerY>>
	 */
	public final Pair<Long, Pair<Long, Long>> UNIVERSE_DIMENSIONS;
	
	public UniverseMap(Vector<Body> bodies, Pair<Long, Pair<Long, Long>> dimensions)
	{
		ALL_BODIES = bodies;
		STAR_SYSTEMS = isolateStarSystems(ALL_BODIES);
		UNIVERSE_DIMENSIONS = dimensions;
	}
	
	private Vector<StarSystem> isolateStarSystems(Vector<Body> bodies)
	{
		Vector<StarSystem> stsystems = new Vector<StarSystem>();
		for (Body bod : bodies)
			if (bod != null && bod.TYPE == Body.BodyType.STAR_SYSTEM)
				stsystems.add((StarSystem) bod);
		
		return stsystems;
	}
	
//	BODY-RETREIVAL METHODS: (E.G. GET N-CLOSEST BODIES, ETC)
	
	/**
	 * Returns all the contents of that star system.
	 * 
	 * @param stsys The {@link StarSystem} in question.
	 * @return A {@link Vector} that contains what's inside the star system.
	 */
	public Vector<Body> getContents(StarSystem stsys)
	{
		return retreiveRecurs(stsys);
	}
	
	/*
	 * Helper for getContents
	 */
	private Vector<Body> retreiveRecurs(Body body) 
	{
		if (body.CHILDREN.isEmpty()) //BASE CASE
			return null;
		
		else //RECURSIVE CASE
		{
			Vector<Body> contents = new Vector<Body>();
			
			for (int child_id : body.CHILDREN)
			{
				//adding the child
				Body child = getById(child_id);
				contents.add(child);
				
				//adding all its contents
				Vector<Body> grand_children = retreiveRecurs(child);
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
		for (int i = 0; i < ALL_BODIES.size(); i++)
			if (ALL_BODIES.get(i).GAME_ID == id)
				return ALL_BODIES.get(i);
		
		//else:
		return null;
	}
	
	
	
	
	/**
	 * Find n closest {@link StarSystem}s to the specified {@link StarSystem} from the whole game-world, and return a {@link Vector} of them.
	 * The {@link Vector} will contain <= n {@link StarSystem}s.
	 */
	public Vector<StarSystem> getNclosestStarSystems(StarSystem ssys, int n)
	{
		return nclosest(ssys, STAR_SYSTEMS, n);
	}
	
	
	/**
	 * Find n closest {@link StarSystem}s to the specified {@link StarSystem} from some collection, and return a {@link Vector} of them.
	 * The {@link Vector} will contain <= n {@link StarSystem}s.
	 */
	public Vector<StarSystem> getNclosestStarSystems(StarSystem ssys, List<StarSystem> collection, int n)
	{
		return nclosest(ssys, collection, n);
	}
	
	
	private Vector<StarSystem> nclosest(StarSystem ssys, List<StarSystem> collection, int n)
	{
		//System.out.println("For star system: " + ssys.NAME);
		
		TreeMap<Long, StarSystem> distance_to_body = new TreeMap<Long, StarSystem>();
		
		//puts all bodies in the map, except for the star system in question
		for (StarSystem s1 : collection)
			if (s1.GAME_ID != ssys.GAME_ID)
				distance_to_body.put(getDistance(s1, ssys), s1);

		
		//finds n-closest, or as long as there are bodies
		Vector<StarSystem> nclosest = new Vector<StarSystem>(n);
		for (int i = 0; i < n && i < collection.size(); i++) //assumption: a star system should exist, as long as i < STAR_SYSTEMS.size().
		{
			long id = distance_to_body.ceilingKey((long)0); //gets the closest distance to 0.
			StarSystem s2 = distance_to_body.get(id);
			nclosest.add(s2);
			distance_to_body.remove(id); //removes the selected body from the object-distance mapping, so it won't be counted twice
		}
		
		return nclosest;
	}
	
	
	/**
	 * Calculates the distance between two {@link Body}s, a and b.
	 * Neither a or b can be null.
	 */
	public long getDistance (Body a, Body b)
	{
		//CALCULATIONS TOO LARGE FOR LONG, CASTING TO DOUBLE:
		double x_dist = shortestDistance(a, b, 'x');
		double y_dist = shortestDistance(a, b, 'y');
		
		return new Double(Math.sqrt(x_dist * x_dist + y_dist * y_dist)).longValue(); //close enough approximation!
	}
	
	private double shortestDistance(Body a, Body b, char axis)
	{
		switch (axis)
		{
			case 'x':
			{
				
			}
			case 'y':
			{
				
			}
		}
	}
	
	/*
	 * side : 'a' up, 'b' down, 'c' left, 'd' right
	 */
	private double distToBoundary(Body b, char side)
	{
		switch (side)
		{
			case 'a': return b.POSITION[1];
			
			case 'b':
				
			case 'c':
			
			case 'd':
		}
	}
	
	/*
	 * side : 'a' up, 'b' down, 'c' left, 'd' right
	 */
	private double universeBoundaryLocation(char side)
	{
		switch (side)
		{
			case 'a': return UNIVERSE_DIMENSIONS.
			
			case 'b':
				
			case 'c':
			
			case 'd':
		}
	}
	
	/*
	 * returns the length of one 
	 */
	private double universeDimension()
	{
		return Math.sqrt(UNIVERSE_DIMENSIONS.left);
	}

}
