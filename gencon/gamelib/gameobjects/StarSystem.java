package gencon.gamelib.gameobjects;

import gencon.gamelib.gameobjects.Body.BodyType;

import java.util.List;

/**
 * A class which represents a star-system in TP-RFTS.
 * 
 * @author Victor Ivri
 *
 */
public class StarSystem extends Body
{

	public StarSystem(int gameId, long modTime, String name, long[] position, int parent, List<Integer> children) 
	{
		super(gameId, modTime, Body.BodyType.STAR_SYSTEM, name, position, parent, children);
	}

}
