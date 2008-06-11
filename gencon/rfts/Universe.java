package gencon.rfts;

import gencon.rfts.Body.BodyType;

import java.util.Vector;

/**
 * A class which represents the universe in TP-RFTS.
 * 
 * @author Victor Ivri
 *
 */
public class Universe extends Body
{
	public Universe(int gameId, long modTime, String name, long[] position)
	{
		super(gameId, modTime, Body.BodyType.UNIVERSE, name, position);
	}

}
