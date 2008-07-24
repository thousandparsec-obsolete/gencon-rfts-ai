package gencon.gamelib.RFTS.gameobjects;


import java.util.List;

/**
 * A class which represents the universe in TP-RFTS.
 * 
 * @author Victor Ivri
 *
 */
public class Universe extends Body
{

	//DUMMY VALUES:
	public final static int UNIVERSE_PARENT = -1; //magic number... 
	private final static long DUMMY_POSITION = 0; //magic number... safe to assume no-one else will be at 0,0,0
	private final static long[] UNIVERSE_POSITION = {DUMMY_POSITION, DUMMY_POSITION, DUMMY_POSITION};
	
	public Universe(int gameId, long modTime, String name, List<Integer> children) 
	{
		super(gameId, modTime, Body.BodyType.UNIVERSE, name, UNIVERSE_POSITION, UNIVERSE_PARENT, children);
	}
	
	public Universe(Universe other)
	{
		super(other);
	}

}
