package gencon.gamelib.RISK;

import gencon.gamelib.RISK.gameobjects.Constellation;
import gencon.gamelib.RISK.gameobjects.Star;
import gencon.gamelib.RISK.gameobjects.RiskGameObject;
import gencon.gamelib.RISK.gameobjects.Wormhole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class UniverseMap 
{
	private Collection<Constellation> constellations;
	private Collection<Star> stars;
	private int myReinforcements;
	
	public UniverseMap(Collection<RiskGameObject> gameObjects)
	{
		constellations = new HashSet<Constellation>();
		stars = new HashSet<Star>();
		init(gameObjects);
	}
	
	/**
	 * Deep copy constructor. 
	 */
	public UniverseMap(UniverseMap other)
	{
		stars = new HashSet<Star>();
		constellations = new HashSet<Constellation>();
		
		for (Star s : other.stars)
			stars.add(new Star(s));
		for (Constellation c : other.constellations)
			constellations.add(new Constellation(c));
	}
	
	/*
	 * sorts the objects, and creates adjacencies in planets.
	 */
	private void init(Collection<RiskGameObject> gameObjects)
	{
		Collection<Wormhole> wormholes = new HashSet<Wormhole>();
		
		//arrange on shelves:
		for (RiskGameObject rgo : gameObjects)
		{
			if (rgo.getClass() == Wormhole.class)
				wormholes.add((Wormhole) rgo);
			else if (rgo.getClass() == Constellation.class)
				constellations.add((Constellation) rgo);
			else if (rgo.getClass() == Star.class)
				stars.add((Star) rgo);
		}
		
		//create adjacencies in planets:
		for (Wormhole wh : wormholes)
		{
			Star a = getStarWithName(wh.END_A);
			Star b = getStarWithName(wh.END_B);
			
			a.addAdjacent(b.GAME_ID);
			b.addAdjacent(a.GAME_ID);
		}
		
		//get reinforcements:
		myReinforcements = 0;
		
	}
	
	
	private Star getStarWithName(String name)
	{
		for (Star s : stars)
			if (s.NAME.equals(name))
				return s;
		
		return null; //if not found; SHOULD NEVER HAPPEN, THOUGH!
	}
	
	public Star getStarWithId(int id)
	{
		Star star = null;
		
		for (Star s : stars)
			if (s.GAME_ID == id)
			{
				star = new Star(s);
				break;
			}
		
		return star;
	}
	
	/**
	 * @return An unsafe reference. Used in game simulation.
	 */
	public Star getStarWithIdUnsafe(int id)
	{
		Star star = null;
		
		for (Star s : stars)
			if (s.GAME_ID == id)
			{
				star = s;
				break;
			}
		
		return star;
	}
	
	/**
	 * @return The distance between two {@link Star}s on the graph. If result = {@link Short}.MAX_VALUE , then no route exists. In a normal map, should never happen!!!
	 */
	public short getDistance(Star a, Star b)
	{
		Collection<Star> traversed = new HashSet<Star>();
		traversed.add(a);
		return getDistRecurs(a, b, traversed, (short)0);
	}
	
	
	private boolean FOUND = false;
	
	private short getDistRecurs(Star current, Star destination, Collection<Star> traversed, short dist)
	{
		if (FOUND) //don't bother if found quicker way!
			return Short.MAX_VALUE;
		
		if (current.equals(destination)) //final case!
		{
			FOUND = true;
			return dist;
		}
		
		else
		{
			short[] distances = new short[current.getAdjacencies().size()];
			
			byte i = 0;
			for (Integer id : current.getAdjacencies())
			{
				Star s = getStarWithId(id);
				if (!traversed.contains(s))
				{
					Collection<Star> newTraversed = new HashSet<Star>(traversed);
					newTraversed.add(s);
					distances[i] = getDistRecurs(s, destination, newTraversed, (short)(dist + 1));
				}
				else 
					distances[i] = Short.MAX_VALUE;
					
				i++;
			}
			
			short minDist = Short.MAX_VALUE;
			for (int j = 0; j < distances.length; j++)
				if (distances[j] < minDist)
					minDist = distances[j];
			
			return minDist;
		}
	}

	/**
	 * @return The 'N' closest {@link Star}s to the specified target.
	 */
	public Collection<Star> getNclosest(Star star, byte N)
	{
		Collection<Star> copied = new HashSet<Star>();
		Collection<Star> unsafeResult = getNclosestUnsafe(star, N);
		
		for (Star s : unsafeResult)
			copied.add(new Star(s));
		
		return copied;
	}
	
	
	/**
	 * @return An unsafe reference. Used in game simulation.
	 */
	public Collection<Star> getNclosestUnsafe(Star star, byte N)
	{
		Collection<Star> collectedSoFar = new HashSet<Star>();
		Random rand = new Random(System.currentTimeMillis());
		
		List<Star> neighbors = new ArrayList<Star>();
		for (Integer id : star.getAdjacencies())
			neighbors.add(getStarWithIdUnsafe(id));
		
		//fill up the collection until reaches right capacity
		while (N != 0)
		{
			List<Star> backupNeighbors = new ArrayList<Star>(neighbors);
			Collection<Integer> beenToIndeces = new HashSet<Integer>();
			
			while (backupNeighbors.size() != 0 || N != 0)
			{
				int r = rand.nextInt(backupNeighbors.size());
				
				if (!beenToIndeces.contains(r))
				{
					collectedSoFar.add(backupNeighbors.get(r));
					N --;
					backupNeighbors.remove(r);
					beenToIndeces.add(r);
				}
			}
			
			//do for next ring of neighbors:
			List<Star> newNeighbors = new ArrayList<Star>();
			
			for (Star s : neighbors)
				for (Integer id : s.getAdjacencies())
				{
					Star s1 = getStarWithIdUnsafe(id);
					if (!newNeighbors.contains(s1))
						newNeighbors.add(s1);
				}
			
			neighbors = newNeighbors;
		}
		
		return collectedSoFar;
	}
	

	/**
	 * @return A deep copy of the {@link Collection} of {@link Constellation}s.
	 */
	public Collection<Constellation> getConstellations()
	{
		Collection<Constellation> copied = new HashSet<Constellation>();
		
		for (Constellation c : constellations)
			copied.add(new Constellation(c));
		
		return copied;
	}
	
	/**
	 * @return An unsafe reference. Used in game simulation.
	 */
	public Collection<Constellation> getConstellationsUnsafe()
	{
		Collection<Constellation> copied = new HashSet<Constellation>();
		
		for (Constellation c : constellations)
			copied.add(c);
		
		return copied;
	}
	
	
	/**
	 * @return A deep copy of the {@link Collection} of {@link Star}s.
	 */
	public Collection<Star> getStars()
	{
		Collection<Star> copied = new HashSet<Star>();
		
		for (Star s : stars)
			copied.add(new Star(s));
		
		return copied;
	}
	
	/**
	 * @return An unsafe reference. Used in game simulation.
	 */
	public Collection<Star> getStarsUnsafe()
	{
		Collection<Star> copied = new HashSet<Star>();
		
		for (Star s : stars)
			copied.add(s);
		
		return copied;
	}
	
	
	public Collection<Star> getStarsOfPlayer(int playerid)
	{
		Collection<Star> copied = new HashSet<Star>();
		
		for (Star s : stars)
			if (s.getOwner() == playerid)
				copied.add(new Star(s));
		
		return copied;
	}
	
	/**
	 * @return An unsafe reference. Used in game simulation.
	 */
	public Collection<Star> getStarsOfPlayerUnsafe(int playerid)
	{
		Collection<Star> copied = new HashSet<Star>();
		
		for (Star s : stars)
			if (s.getOwner() == playerid)
				copied.add(s);
		
		return copied;
	}

	
	public int getMyReinforcements()
	{
		return myReinforcements;
	}
	
	public void setMyReinforcements(int reinforcements)
	{
		myReinforcements = reinforcements;
	}
}
