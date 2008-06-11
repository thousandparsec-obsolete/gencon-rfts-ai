package gencon.rfts;

import gencon.rfts.Body.BodyType;

/**
 * A class which represents a star-system in TP-RFTS.
 * 
 * @author Victor Ivri
 *
 */
public class StarSystem extends Body
{
	public StarSystem(int gameId, long modTime, String name, long[] position) 
	{
		super(gameId, modTime, Body.BodyType.STAR_SYSTEM, name, position);
	}

}
