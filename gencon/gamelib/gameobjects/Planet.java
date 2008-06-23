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
		public final short COLONIST;
		public final short SHIP_TECH;
		public final short PDB1S;
		public final short PDB1_MAINT;
		public final short PDB2S;
		public final short PDB2_MAINT;
		public final short PDB3S;
		public final short PDB3_MAINT;
		
		public Resources(short resource_pts, short industry, short population, short social_env,
				short planetary_env, short pop_maintanance, short colonist, short ship_tech, short pdb1, short pdb1_m,
				short pdb2, short pdb2_m, short pdb3, short pdb3_m)
		{
			RESOURCE_POINTS = resource_pts;
			INDUSTRY = industry;
			POPULATION = population;
			SOCIAL_ENV = social_env;
			PLANETARY_ENV = planetary_env;
			POP_MAINTANENCE = pop_maintanance;
			COLONIST = colonist;
			SHIP_TECH = ship_tech;
			PDB1S = pdb1;
			PDB1_MAINT = pdb1_m;
			PDB2S = pdb2;
			PDB2_MAINT = pdb2_m;
			PDB3S = pdb3;
			PDB3_MAINT = pdb3_m;
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
