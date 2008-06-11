package gencon.rfts;

import java.util.Vector;

/**
 * A class which represents planets in TP-RFTS.
 * 
 * @author Victor Ivri
 *
 */
public class Planet extends Body 
{
	private Player owner;
	private Vector<Order> orders;
	private Resources resources;


	public Planet(int gameId, long modTime, String name, long[] position, Player owner) 
	{
		super(gameId, modTime, Body.BodyType.PLANET, name, position);
		setOwner(owner);
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
		
		return returnedList;
	}

	/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * OWNER
	 */
	
	public Player getOwner()
	{
		return owner;
	}
	
	public void setOwner(Player other)
	{
		owner = other;
	}
	
	/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * RESOURCES
	 */
	
	/**
	 * Deep copy of the Planet's {@link Resources}.
	 */
	public Resources getResources()
	{
		return new Resources(resources);
	}
	
	/**
	 * Sets the specified resource to a new quantity.
	 * @param type One of {@link Resources.ResourceType}.
	 * @param amount The quantity to be set.
	 */
	public void changeResource(Resources.ResourceType type, int quantity)
	{
		resources.setResource(type, quantity);
	}
}
