package gencon.robolib.RFTS;

import gencon.gamelib.RFTS.FullGameStatusRFTS;
import gencon.gamelib.RFTS.UniverseMapRFTS;
import gencon.gamelib.RFTS.gameobjects.Body;
import gencon.gamelib.RFTS.gameobjects.Fleet;
import gencon.gamelib.RFTS.gameobjects.Planet;
import gencon.gamelib.RFTS.gameobjects.StarSystem;
import gencon.robolib.RFTS.AdvancedMap.Sectors.Sector;

import java.util.Collection;
import java.util.HashSet;


/**
 * A set of methods, which enhance on the functionality of the basic {@link UniverseMapRFTS}.
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
	private final FullGameStatusRFTS FGS;
	
	public final Sectors SECTORS;
	
	public final static char[] SECTOR_NAMES = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i'};
	
	public AdvancedMap(FullGameStatusRFTS full_game_status)
	{
		FGS = full_game_status;
		SECTORS = createSectors();
		fillSectors();
	}
	
	private Sectors createSectors()
	{
		Collection<Sectors.Sector> sectors = new HashSet<Sectors.Sector>();
		for (int i = 0; i < SECTOR_NAMES.length; i++)
			sectors.add(new Sectors.Sector(SECTOR_NAMES[i]));
		
		return new Sectors(sectors);
	}
	
	/*
	 * Assigns the star systems to one of 9 sectors:
	 */
	private void fillSectors()
	{
		//set boundaries
		double top = getBasicMap().BOUNDARIES.right.right;
		double bottom = getBasicMap().BOUNDARIES.right.left;
		double left = getBasicMap().BOUNDARIES.left.left;
		double right = getBasicMap().BOUNDARIES.left.right;
		
		
		//cut the map:
		double height = top - bottom;
		double width = right - left;
		
		double h1 = (height / 3) + bottom;
		double h2 = (height / 3) * 2 + bottom;
		
		double w1 = (width / 3) + left;
		double w2 = (width / 3) * 2 + left;
		
		//assign to sectors:
		Collection<StarSystem> starSystems = getBasicMap().STAR_SYSTEMS;
		for (StarSystem ss : starSystems)
		{
			long x = ss.POSITION[0];
			long y = ss.POSITION[1];
			
			if (x < w1)
			{
				if (y < h1)
					SECTORS.getById('g').addToContents(ss.GAME_ID);
				else if (y < h2)
					SECTORS.getById('d').addToContents(ss.GAME_ID);
				else
					SECTORS.getById('a').addToContents(ss.GAME_ID);
			}
			else if (x < w2)
			{
				if (y < h1)
					SECTORS.getById('h').addToContents(ss.GAME_ID);
				else if (y < h2)
					SECTORS.getById('e').addToContents(ss.GAME_ID);
				else
					SECTORS.getById('b').addToContents(ss.GAME_ID);
			}
			else
			{
				if (y < h1)
					SECTORS.getById('i').addToContents(ss.GAME_ID);
				else if (y < h2)
					SECTORS.getById('f').addToContents(ss.GAME_ID);
				else
					SECTORS.getById('c').addToContents(ss.GAME_ID);
			}
		}
	}
	
	/**
	 * @return A deep copy of the underlying {@link UniverseMapRFTS}, to avoid the need to store it separately.
	 */
	public UniverseMapRFTS getBasicMap()
	{
		return (UniverseMapRFTS)FGS.getCurrentStatus().left;
	}
	
	/**
	 * 
	 * @return A {@link Collection} of all the {@link Planet} the player owns, in a particular {@link Sector}.
	 */
	public Collection<Planet> getMyPlanets(Sector sector)
	{
		int myNum = FGS.getCurrentStatus().right.getMe().NUM;
		
		Collection<Integer> ssystems = sector.getContents();
		Collection<Planet> myplanets = new HashSet<Planet>();
		
		for (Integer i : ssystems)
		{
			Collection<Body> orbiting = getBasicMap().getContents((StarSystem)getBasicMap().getById(i.intValue()));
			if (orbiting != null)
				for (Body b : orbiting)
					if (b.TYPE == Body.BodyType.PLANET)
					{
						Planet p = (Planet)b;
						if (p.OWNER == myNum)
							myplanets.add(p);
					}
		}
		
		return myplanets;
	}
	
	/**
	 * @return A {@link Collection} of all the {@link Fleet} the player owns in a certain {@link Sector}.
	 */
	public Collection<Fleet> getAllMyFleet(Sector sector)
	{
		int myNum = FGS.getCurrentStatus().right.getMe().NUM;
		
		Collection<Integer> ssystems = sector.getContents();
		Collection<Fleet> myfleet = new HashSet<Fleet>();
		
		for (Integer i : ssystems)
		{
			Collection<Body> orbiting = getBasicMap().getContents((StarSystem)getBasicMap().getById(i.intValue()));
			if (orbiting != null)
				for (Body b : orbiting)
					if (b.TYPE == Body.BodyType.FLEET)
					{
						Fleet f = (Fleet)b;
						if (f.OWNER == myNum)
							myfleet.add(f);
					}
		}
		
		return myfleet;
	}
	
	
	/**
	 * @return A subset of the {@link Collection} in getAllMyFleet(Sector), 
	 * s.t. each {@link Fleet} in the set will have no orders on it.
	 */
	public Collection<Fleet> getAllIdleFleet(Sector sector)
	{
		Collection<Fleet> myFleet = getAllMyFleet(sector);
		
		Collection<Fleet> idleFleet = new HashSet<Fleet>();
		
		for(Fleet f : myFleet)
			if (f.ORDERS == 0)
				idleFleet.add(f);
		
		return idleFleet;
	}
	
	
	
	
	static class Sectors
	{
		public final Collection<Sector> SECTORS;
		
		public Sectors(Collection<Sector> sectors)
		{
			SECTORS = new HashSet<Sector>(sectors);
		}
		
		public Sector getById(char id)
		{
			Sector sec = null;
			
			for (Sector s : SECTORS)
				if (s.ID == id)
					sec = s;
			
			return sec;
		}
		
		public char contains(StarSystem ss) throws Exception
		{
			for (Sector sec : SECTORS)
				if (sec.contains(ss))
					return sec.ID;
			
			//else:
			throw new Exception("Unexpected result: StarSystem is not assigned to any sector.");
		}
		
		static class Sector
		{
			public static enum State
			{
				STRONGHOLD, PERIPHERY, NEUTRAL, STR_HOSTILE, WEAK_HOSTILE, UNEXPLORED;
			}
			
			public final char ID;
			private Collection<Integer> contents;
			private State state;
			private boolean under_threat;
			private boolean under_attack;
			
			public Sector(char id)
			{
				ID = id;
				contents = new HashSet<Integer>();
			}
			
			public boolean contains(StarSystem ss)
			{
				return contents.contains(new Integer(ss.GAME_ID));
			}
			
			public void addToContents(Integer ssId)
			{
				contents.add(new Integer(ssId));
			}
			
			public Collection<Integer> getContents()
			{
				return new HashSet<Integer>(contents);
			}
			
			public void setThreat(boolean value)
			{
				under_threat = value;
			}
			
			public boolean getThreat()
			{
				return under_threat;
			}
			
			public void setUnderAttack(boolean value)
			{
				under_attack = value;
			}
			
			public boolean getUnderAttack()
			{
				return under_attack;
			}

			public State getState() {
				return state;
			}

			public void setState(State state) {
				this.state = state;
			}
			
		}
		
	}
}
