package gencon.gamelib.RFTS.gameobjects;

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
	public final int OWNER;
	public final int ORDERS;
	public final Resources RESOURCES;

	public Planet(int gameId, long modTime, String name, long[] position, int parent, 
			List<Integer> children, int owner, int orders, Resources resources) 
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
}
