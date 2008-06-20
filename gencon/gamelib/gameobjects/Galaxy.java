package gencon.gamelib.gameobjects;


import java.util.List;

/**
 * A class which represents a galaxy in TP-RFTS.
 * 
 * @author Victor Ivri
 *
 */
public class Galaxy extends Body
{
	public Galaxy(int gameId, long modTime, String name, long[] position, int parent, List<Integer> children) 
	{
		super(gameId, modTime, Body.BodyType.GALAXY, name, position, parent, children);
	}
	
	public Galaxy(Galaxy other)
	{
		super(other);
	}
}
