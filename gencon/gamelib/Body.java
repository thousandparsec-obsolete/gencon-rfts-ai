package gencon.gamelib;

import java.util.List;

/**
 * A template for bodies populating the game-world.
 * All fields are final, for the reason that new Bodies will be created each turn.
 * 
 * @author Victor Ivri
 *
 */
public class Body 
{
	public final int GAME_ID;
	public final BodyType TYPE;
	public final String NAME;
	public final int PARENT;
	public final List<Integer> CHILDREN;
	public final long[] POSITION;
	public final long MODTIME;
	
	/**
	 * The types of Bodies present in the game.
	 */
	public static enum BodyType
	{
		UNIVERSE, GALAXY, STAR_SYSTEM, PLANET, FLEET;
	}
	
	/**
	 * 
	 * @param gameId
	 * @param modTime
	 * @param type
	 * @param name
	 * @param position
	 * @param parent
	 * @param children
	 */
	public Body(int gameId, long modTime, BodyType type, String name, long[] position, int parent, List<Integer> children)
	{
		GAME_ID = gameId;
		TYPE = type;
		NAME = name;
		PARENT = parent;
		CHILDREN = children;
		POSITION = position;
		MODTIME = modTime;
	}
}