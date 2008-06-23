package gencon.gamelib;

import gencon.gamelib.gameobjects.Body;
import gencon.gamelib.gameobjects.StarSystem;

import java.util.TreeMap;
import java.util.Vector;

/**
 * A representation of the game-world based on star-systems. 
 * 
 * @author Victor Ivri
 */
public class UniverseMap 
{
	public final Vector<StarSystem> STAR_SYSTEMS;
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
	 * Find n closest game-objects to the specified {@link Body}, and return a {@link Vector} of them.
	 * The {@link Vector} will contain <= n {@link Body}s.
	 */
	public Vector<Body> getNclosestBodies(Body body, int n)
	{
		TreeMap<Long, Body> distance_to_body = new TreeMap<Long, Body>();
		
		//puts all bodies in the map
		for (Body b1 : ALL_BODIES)
			if (b1 != null)
			distance_to_body.put(getDistance(b1, body), b1);
		
		//finds n-closest, or as long as there are bodies
		Vector<Body> nclosest = new Vector<Body>(n);
		for (int i = 0; i < n && i < ALL_BODIES.size(); i++) //assumption: a body should exist, as long as i < ALL_BODIES.size().
		{
			long id = distance_to_body.ceilingKey((long)0); //gets the closest distance to 0.
			Body b2 = distance_to_body.get(id);
			nclosest.add(b2);
			distance_to_body.remove(b2); //removes the selected body from the object-distance mapping 
		}
		
		return nclosest;
	}
	
	
	
	/**
	 * Find n closest {@link StarSystem}s to the specified {@link Body}, and return a {@link Vector} of them.
	 * The {@link Vector} will contain <= n {@link StarSystem}s.
	 */
	public Vector<StarSystem> getNclosestStarSys(StarSystem ssys, int n)
	{
		TreeMap<Long, StarSystem> distance_to_body = new TreeMap<Long, StarSystem>();
		
		//puts all bodies in the map
		for (StarSystem s1 : STAR_SYSTEMS)
			if (s1 != null)
			distance_to_body.put(getDistance(s1, ssys), s1);
		
		//finds n-closest, or as long as there are bodies
		Vector<StarSystem> nclosest = new Vector<StarSystem>(n);
		for (int i = 0; i < n && i < ALL_BODIES.size(); i++) //assumption: a body should exist, as long as i < ALL_BODIES.size().
		{
			long id = distance_to_body.ceilingKey((long)0); //gets the closest distance to 0.
			StarSystem s2 = distance_to_body.get(id);
			nclosest.add(s2);
			distance_to_body.remove(s2); //removes the selected body from the object-distance mapping 
		}
		
		return nclosest;
	}
	
	
	/**
	 * Calculates the distance between two {@link Body}s, a and b.
	 * Neither a or b can be null.
	 */
	public long getDistance (Body a, Body b)
	{
		long x_dist = a.POSITION[0] - b.POSITION[0];
		long y_dist = a.POSITION[1] - b.POSITION[1];
		long z_dist = a.POSITION[2] - b.POSITION[2];
		
		return new Double(Math.sqrt(x_dist^2 + y_dist^2 + z_dist^2)).longValue(); //a very close approximation.. will do!
	}
}
