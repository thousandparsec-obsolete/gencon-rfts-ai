package gencon.gamelib.gameobjects;

import gencon.gamelib.Game_Player;

import java.util.List;
import java.util.Vector;

/**
 * A class which represents a galaxy in TP-RFTS.
 * 
 * @author Victor Ivri
 *
 */
public class Fleet extends Body
{
	public final Game_Player OWNER;
	public final FleetOrders ORDERS;
	public final long[] SPEED;
	public final Vector<Ship> SHIPS;
	public final float DAMAGE; //not sure how it would show.

	public Fleet(int gameId, long modTime, String name, long[] position, Game_Player owner, 
			int parent, List<Integer> children, float damage, 
			Vector<Ship> ships, FleetOrders orders, long[] speed) 
	{
		super(gameId, modTime, Body.BodyType.FLEET, name, position, parent, children);
		OWNER = owner;
		DAMAGE = damage;
		ORDERS = orders;
		SHIPS = ships;
		SPEED = speed;
	}
	
	/**
	 * 
	 * @return The net velocity of the fleet in the 3D space (in RFTS, space is only 2D, but the extra calculation is still there (perhaps for future implementations)).
	 */
	public double getActualVelocity()
	{
		return Math.pow(SPEED[0]^2 + SPEED[1]^2 + SPEED[2]^2, -3);
	}
	
	/**
	 * The type of ships in RFTS. 
	 */
	public static enum ShipType
	{
		TRANSPORT_COLONIST, SCOUT, MK1, MK2, MK3, MK4;
	}
	
	/**
	 * A ship in the Fleet.
	 * 
	 * @author Victor Ivri
	 */
	public class Ship
	{	
		public final ShipType TYPE;
		
		public Ship(ShipType type)
		{
			TYPE = type;
		}
	}
	

	public class FleetOrders
	{
		public final Vector<FOrder> ORDERS;
		
		public FleetOrders(Vector<FOrder> orders)
		{
			ORDERS = orders;
		}
		
		public class FOrder extends Order
		{
			public FOrder(int order_type, int object_id, int place_in_queue) 
			{
				super(order_type, object_id, place_in_queue);
			}
		}
	}
}
