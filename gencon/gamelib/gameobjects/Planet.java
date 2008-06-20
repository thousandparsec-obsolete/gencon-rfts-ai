package gencon.gamelib.gameobjects;

import gencon.gamelib.Game_Player;

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
	public final PlanetOrders ORDERS;
	public final Resources RESOURCES;

	public Planet(int gameId, long modTime, String name, long[] position, int parent, 
			List<Integer> children, Game_Player owner, PlanetOrders orders, Resources resources) 
	{
		super(gameId, modTime, Body.BodyType.PLANET, name, position, parent, children);
		OWNER = owner;
		ORDERS = orders;
		RESOURCES = resources;
	}	
	
	public Planet(Planet p)
	{
		this(p.GAME_ID, p.MODTIME, p.NAME, p.POSITION, p.PARENT, p.CHILDREN, p.OWNER, p.ORDERS, p.RESOURCES);
	}

	/**
	 * Stores the resources available on {@link Planet}s in RFTS.
	 *
	 * @author Victor Ivri
	 */
	public class Resources 
	{
		public final short RESOURCE_POINTS;
		public final short INDUSTRY;
		public final short POPULATION;
		public final short SOCIAL_ENV;
		public final short PLANETARY_ENV;
		public final short POP_MAINTANENCE;
		public final short PDBS;
		
		public Resources(short resource_pts, short industry, short population, short social_env,
				short planetary_env, short pop_maintanance, short pdbs)
		{
			RESOURCE_POINTS = resource_pts;
			INDUSTRY = industry;
			POPULATION = population;
			SOCIAL_ENV = social_env;
			PLANETARY_ENV = planetary_env;
			POP_MAINTANENCE = pop_maintanance;
			PDBS = pdbs;
		}
	}
	
	public class PlanetOrders
	{
		public final Vector<POrder> ORDERS;
		
		public PlanetOrders(Vector<POrder> orders)
		{
			ORDERS = orders;
		}
		
		public class POrder extends Order
		{
			public POrder(int order_type, int object_id, int place_in_queue) 
			{
				super(order_type, object_id, place_in_queue);
			}
		}
	}
}
