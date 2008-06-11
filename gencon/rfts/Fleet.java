package gencon.rfts;

import java.util.Vector;

public class Fleet extends Body
{
	public final Player OWNER;
	private Vector<Order> orders;
	private long[] speed = new long[3];
	private Vector<Ship> ships;

	public Fleet(int gameId, long modTime, String name, long[] position, Player owner) 
	{
		super(gameId, modTime, Body.BodyType.FLEET, name, position);
		OWNER = owner;
	}
	
	/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * ORDERS
	 */
	
	/**
	 * @param order Give a new {@link Order} to the Planet.
	 */
	public void giveOrder(Order order)
	{
		orders.add(order);
	}
	
	/**
	 * @return a deep copy-constructor of the {@link Order}s given to the Planet.
	 */
	public Vector<Order> getOrders()
	{
		Vector<Order> returnedList;
		for (Order o : orders)
			returnedList.add(new Order(o));
	}
	
	/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * SPEED
	 */
	
	/**
	 * 
	 * @return The speed of the fleet, in x, y, z components, respectively.
	 */
	public long[] getSpeed()
	{
		return speed;
	}
	
	/**
	 * 
	 * @return The net velocity of the fleet in the 3D space (in RFTS, space is only 2D, but the extra calculation is still there (perhaps for future implementations)).
	 */
	public double getActualVelocity()
	{
		return Math.pow(speed[0]^2 + speed[1]^2 + speed[2]^2, -3);
	}
	
	/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * SHIPS
	 */
	
	public Vector<Ship> getShips()
	{
		return ships;
	}
	

	public void setShips(Vector<Ship> shipList)
	{
		ships = shipList;
	}
	
	
	public enum ShipType
	{
		TRANSPORT_COLONIST, SCOUT, MK1, MK2, MK3, MK4;
	}
	
	public class Ship
	{	
		public final ShipType TYPE;
		
		public Ship(ShipType type)
		{
			TYPE = type;
		}
	}
}
