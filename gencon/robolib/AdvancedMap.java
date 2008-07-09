package gencon.robolib;

import gencon.gamelib.FullGameStatus;
import gencon.gamelib.UniverseMap;
import gencon.gamelib.gameobjects.Body;
import gencon.gamelib.gameobjects.Fleet;
import gencon.gamelib.gameobjects.Planet;
import gencon.gamelib.gameobjects.StarSystem;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * A set of methods, which enhance on the functionality of the basic {@link UniverseMap}.
 * The most notable addition is the division of the game-world into 'sectors', 
 * which are arranged like so:
 *  -------
 * | a b c |
 * | d e f |
 * | g h i |
 *  -------
 * 
 * @author Victor Ivri
 *
 */
public class AdvancedMap 
{
	private FullGameStatus fgs;
	private Collection<Sector> sectors;
	
	public AdvancedMap(FullGameStatus full_game_status)
	{
		fgs = full_game_status;
		createSectors();
		assignSectors();
	}
	
	/*
	 * Assigns the star systems to one of 9 sectors:
	 */
	private void assignSectors()
	{
		List<StarSystem> starSystems = getBasicMap().STAR_SYSTEMS;
		
		// --------------------------------
		//finding topmost:
		long top = Long.MIN_VALUE;
		for (StarSystem ss : starSystems)
			if (ss.POSITION[1] > top)
				top = ss.POSITION[1];
		
		//finding bottom-most:
		long bottom = Long.MAX_VALUE;
		for (StarSystem ss : starSystems)
			if (ss.POSITION[1] < bottom)
				bottom = ss.POSITION[1];
		
		//finding leftmost:
		long left = Long.MAX_VALUE;
		for (StarSystem ss : starSystems)
			if (ss.POSITION[0] < left)
				left = ss.POSITION[0];
		
		//finding rightmost:
		long right = Long.MIN_VALUE;
		for (StarSystem ss : starSystems)
			if (ss.POSITION[0] > right)
				right = ss.POSITION[0];
		//-------------------------------------
		
		
		//cut the map:
		long height = top - bottom;
		long width = right - left;
		
		long h1 = (height / 3) + bottom;
		long h2 = (2 * h1) + bottom;
		
		long w1 = (width / 3) + left;
		long w2 = (2 * w1) + left;
		
		
		//assign to sectors:
		for (StarSystem ss : starSystems)
		{
			long x = ss.POSITION[0];
			long y = ss.POSITION[1];
			
			if (x < w1)
			{
				if (y < h1)
					sector_G.add(ss.GAME_ID);
				else if (y < h2)
					sector_D.add(ss.GAME_ID);
				else
					sector_A.add(ss.GAME_ID);
			}
			else if (x < w2)
			{
				if (y < h1)
					sector_H.add(ss.GAME_ID);
				else if (y < h2)
					sector_E.add(ss.GAME_ID);
				else
					sector_B.add(ss.GAME_ID);
			}
			else
			{
				if (y < h1)
					sector_I.add(ss.GAME_ID);
				else if (y < h2)
					sector_F.add(ss.GAME_ID);
				else
					sector_C.add(ss.GAME_ID);
			}
		}
		
		
	}
	
	
	/**
	 * 
	 * @return A deep copy of the underlying {@link UniverseMap}, to avoid the need to store it separately.
	 */
	public UniverseMap getBasicMap()
	{
		return fgs.getCurrentStatus().left;
	}
	
	
	/**
	 * @param sector One of 9 sectors in the game-world.
	 * The parameter is case-sensitive (e.g. pass 'a', not 'A').
	 * 
	 * @return A {@link Collection} of all {@link StarSystem}s in that sector.
	 */
	public Collection<StarSystem> getSector(char sector)
	{
		Collection<Integer> contents = null;
		
		switch (sector)
		{
			case 'a': contents = new HashSet<Integer>(sector_A);
			case 'b': contents = new HashSet<Integer>(sector_B);
			case 'c': contents = new HashSet<Integer>(sector_C);
			case 'd': contents = new HashSet<Integer>(sector_D);
			case 'e': contents = new HashSet<Integer>(sector_E);
			case 'f': contents = new HashSet<Integer>(sector_F);
			case 'g': contents = new HashSet<Integer>(sector_G);
			case 'h': contents = new HashSet<Integer>(sector_H);
			case 'i': contents = new HashSet<Integer>(sector_I);
		}
		
		Collection<StarSystem> systems = new HashSet<StarSystem>();
		
		UniverseMap um = getBasicMap();
		for (Integer i : contents)
			systems.add((StarSystem)um.getById(i.intValue()));
			
		return systems;
	}
	
	
	/**
	 * Queries in which sector the {@link StarSystem} is.
	 * 
	 * @param ss The {@link StarSystem} in question.
	 * @return a char to indicate the sector it's in, or throws an {@link Exception} if it belongs to none.
	 */
	public char belongsToSector(StarSystem ss) throws Exception
	{
		if (sector_A.contains(new Integer(ss.GAME_ID)))
			return 'a';
		else if (sector_B.contains(new Integer(ss.GAME_ID)))
			return 'b';
		else if (sector_C.contains(new Integer(ss.GAME_ID)))
			return 'c';
		else if (sector_D.contains(new Integer(ss.GAME_ID)))
			return 'd';
		else if (sector_E.contains(new Integer(ss.GAME_ID)))
			return 'e';
		else if (sector_F.contains(new Integer(ss.GAME_ID)))
			return 'f';
		else if (sector_G.contains(new Integer(ss.GAME_ID)))
			return 'g';
		else if (sector_H.contains(new Integer(ss.GAME_ID)))
			return 'h';
		else if (sector_I.contains(new Integer(ss.GAME_ID)))
			return 'i';
		
		else 
			throw new Exception("Error: Star System " + ss.GAME_ID + " belongs to no sector!");
	}

	
	
	
	
	
	
	/**
	 * 
	 * @return A {@link Collection} of all the {@link Planet} the player owns.
	 */
	public Collection<Planet> getMyPlanets()
	{
		UniverseMap um = fgs.getCurrentStatus().left;
		int myNum = fgs.getCurrentStatus().right.getMe().NUM;
		
		Set<Planet> myplanets = new HashSet<Planet>();
		for (Body b : um.ALL_BODIES)
			if (b.TYPE == Body.BodyType.PLANET)
			{
				Planet p = (Planet)b;
				if (p.OWNER == myNum)
					myplanets.add(p);
			}
		
		return myplanets;
	}
	
	/**
	 * @return A {@link Collection} of all the {@link Fleet} the player owns.
	 */
	public Collection<Fleet> getAllMyFleet()
	{
		UniverseMap um = fgs.getCurrentStatus().left;
		int myNum = fgs.getCurrentStatus().right.getMe().NUM;
		
		Set<Fleet> myfleet = new HashSet<Fleet>();
		for (Body b : um.ALL_BODIES)
			if (b.TYPE == Body.BodyType.FLEET)
			{
				Fleet f = (Fleet)b;
				if (f.OWNER == myNum)
					myfleet.add(f);
			}
		
		return myfleet;
	}
	
	
	/**
	 * @return A subset of the {@link Collection} in getAllMyFleet(), 
	 * s.t. each {@link Fleet} in the set will have no orders on it.
	 */
	public Collection<Fleet> getAllIdleFleet()
	{
		Collection<Fleet> myFleet = getAllMyFleet();
		
		Collection<Fleet> idleFleet = new HashSet<Fleet>();
		
		for(Fleet f : myFleet)
			if (f.ORDERS == 0)
				idleFleet.add(f);
		
		return idleFleet;
	}
	
	
	
	private static class Sector
	{
		public static enum State
		{
			STRONGHOLD, PERIPHERY, NEUTRAL, STR_HOSTILE, WEAK_HOSTILE, UNEXPLORED;
		}
		
		public final char NAME;
		private Collection<StarSystem> contents;
		private State state;
		private boolean under_threat;
		private boolean under_attack;
		
		public Sector(char name)
		{
			NAME = name;
		}
		
		public void setContents(Collection<StarSystem> systems)
		{
			contents = new HashSet<StarSystem>(systems);
		}
		
		public Collection<StarSystem> getContents()
		{
			return new HashSet<StarSystem>(contents);
		}
		
		public void setThreat(boolean value)
		{
			under_threat = value;
		}
		
		public boolean getThreat()
		{
			return under_threat;4
			
		}
		
		
	}
}
