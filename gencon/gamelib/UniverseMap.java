package gencon.gamelib;

import gencon.gamelib.gameobjects.Body;
import gencon.gamelib.gameobjects.StarSystem;
import gencon.gamelib.gameobjects.Universe;

import java.util.TreeMap;
import java.util.Vector;

/**
 * A representation of the game-world based on star-systems. 
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
	
	public UniverseMap(Vector<Body> bodies)
	{
		ALL_BODIES = bodies;
		STAR_SYSTEMS = isolateStarSystems(ALL_BODIES);
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
	 * Find n closest {@link StarSystem}s to the specified {@link StarSystem}, and return a {@link Vector} of them.
	 * The {@link Vector} will contain <= n {@link StarSystem}s.
	 */
	public Vector<StarSystem> getNclosestStarSystems(StarSystem ssys, int n)
	{
		//System.out.println("For star system: " + ssys.NAME);
		
		TreeMap<Long, StarSystem> distance_to_body = new TreeMap<Long, StarSystem>();
		
		//puts all bodies in the map, except for the star system in question, or the universe itself
		//System.out.println("Getting distances from all others:");
		for (StarSystem s1 : STAR_SYSTEMS)
			if (s1 != null && s1.GAME_ID != ssys.GAME_ID && s1.TYPE != Body.BodyType.UNIVERSE)
			{
				distance_to_body.put(getDistance(s1, ssys), s1);
		//		System.out.println("S.Sys: " + s1.NAME + " Pos: " + s1.POSITION[0] + "-" + s1.POSITION[1] + "-" + s1.POSITION[2] + " dist: " + getDistance(s1, ssys));
			}
		
		//finds n-closest, or as long as there are bodies
		//System.out.println("finding " + n + " closest:");
		Vector<StarSystem> nclosest = new Vector<StarSystem>(n);
		for (int i = 0; i < n && i < STAR_SYSTEMS.size(); i++) //assumption: a star system should exist, as long as i < STAR_SYSTEMS.size().
		{
			long id = distance_to_body.ceilingKey((long)0); //gets the closest distance to 0.
			StarSystem s2 = distance_to_body.get(id);
			nclosest.add(s2);
			//System.out.println(i + ") " + s2.NAME + " dist: " + id);
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
		double x_dist = a.POSITION[0] - b.POSITION[0];
		double y_dist = a.POSITION[1] - b.POSITION[1];
		double z_dist = a.POSITION[2] - b.POSITION[2];
		
		//System.out.println(x_dist * x_dist + y_dist * y_dist + z_dist *  z_dist); //
		return new Double(Math.sqrt(x_dist * x_dist + y_dist * y_dist + z_dist *  z_dist)).longValue(); //close enough approximation!
	}
}
