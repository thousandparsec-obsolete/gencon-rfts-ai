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
