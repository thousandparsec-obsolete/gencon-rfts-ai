package gencon.gamelib;

import gencon.gamelib.Players.Game_Player;

import java.util.List;
import java.util.Vector;

/**
 * A class which represents planets in TP-RFTS.
 * 
 * @author Victor Ivri
 *
 */
public class Planet extends Body 
{
	public final Game_Player OWNER;
	public final Vector<Order> ORDERS;
	public final Resources RESOURCES;
	public final Vector<PDB> PDBS;

	public Planet(int gameId, long modTime, String name, long[] position, int parent, 
			List<Integer> children, Game_Player owner, Vector<Order> orders, Resources resources, Vector<PDB> pdbs) 
	{
		super(gameId, modTime, Body.BodyType.PLANET, name, position, parent, children);
		OWNER = owner;
		ORDERS = orders;
		RESOURCES = resources;
		PDBS = pdbs;
	}	

	/**
	 * Stores the resources available on {@link Planet}s in RFTS.
	 *
	 * @author Victor Ivri
	 */
	public class Resources 
	{
		public final int RESOURCE_POINTS;
		public final int INDUSTRY;
		public final int POPULATION;
		public final int SOCIAL_ENV;
		public final int PLANETARY_ENV;
		public final int POP_MAINTANENCE;
		
		public Resources(int resource_pts, int industry, int population, int social_env,
				int planetary_env, int pop_maintanance)
		{
			RESOURCE_POINTS = resource_pts;
			INDUSTRY = industry;
			POPULATION = population;
			SOCIAL_ENV = social_env;
			PLANETARY_ENV = planetary_env;
			POP_MAINTANENCE = pop_maintanance;
		}
	}
	
	
	/**
	 * A class representing Planetary Defence Base.
	 * 
	 * @author Victor Ivri
	 */
	public class PDB
	{
		public final short LEVEL;
		
		public PDB(short level)
		{
			LEVEL = level;
		}
	}
}
