package gencon.rfts;

/**
 * A class which represents a galaxy in TP-RFTS.
 * 
 * @author Victor Ivri
 *
 */
public class Galaxy extends Body
{
	public Galaxy(int gameId, long modTime, String name, long[] position) 
	{
		super(gameId, modTime, Body.BodyType.GALAXY, name, position);
	}

}
