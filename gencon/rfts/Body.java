package gencon.rfts;

/**
 * A template for bodies populating the game-world.
 * 
 * @author Victor Ivri
 *
 */
public class Body 
{
	public final int GAME_ID;
	public final BodyType TYPE;
	public final String NAME;
	private long[] position;
	private long modtime;
	
	/**
	 * The types of Bodies present in the game.
	 */
	public static enum BodyType
	{
		UNIVERSE, GALAXY, STAR_SYSTEM, PLANET, FLEET;
	}
	
	
/**
 * The constrictor of a new Body.
 * 
 * @param gameId
 * @param modTime
 * @param type
 * @param name
 * @param position
 */
	public Body(int gameId, long modTime, BodyType type, String name, long[] position)
	{
		GAME_ID = gameId;
		TYPE = type;
		NAME = name;
		setModtime(modTime);
		setPosition(position);
	}
	
	/**
	 * @return The position of this object in the universe in x,y,z components.
	 */
	public long[] getPosition()
	{
		return position;
	}
	
	/**
	 * @param new_position Sets the position to the value. 
	 * new_position has the position in x, y, z components, respectively.
	 */
	public void setPosition(long[] new_position)
	{
		position = new_position;
	}
	
	/**
	 * 
	 * @param mod_time The time it was last modified.
	 */
	public void setModtime(long mod_time)
	{
		modtime = mod_time;
	}
	
	/**
	 * 
	 * @return The time it was last modified.
	 */
	public long getModtime()
	{
		return modtime;
	}
	
	
}
