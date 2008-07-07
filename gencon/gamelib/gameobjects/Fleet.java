package gencon.gamelib.gameobjects;

import gencon.gamelib.Game_Player;

import java.util.List;

/**
 * A class which represents a galaxy in TP-RFTS.
 * 
 * @author Victor Ivri
 *
 */
public class Fleet extends Body
{
	public final int OWNER;
	public final int ORDERS;
	public final long[] SPEED;
	public final Ships SHIPS;
	public final float DAMAGE; //not sure how it would show.

	public Fleet(int gameId, long modTime, String name, long[] position, int owner, 
			int parent, List<Integer> children, float damage, 
			Ships ships, int orders, long[] speed) 
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


}
