package gencon.robolib;

import gencon.clientLib.Client;

/**
 * The AI robot, which will play the game.
 * 
 * @author Victor Ivri
 * 
 */
public class Robot 
{
	/**
	 * The behavioral characteristics of the robot, defined by a {@link Character}. 
	 */
	public final Character CHARACTER;
	
	public final Client CLIENT;
	
	public final short DIFFICULTY;
	
	
	
	public Robot(String character_classpath, short difficulty, Client client)
	{
		CHARACTER = new Character(character_classpath);
		CLIENT = client;
		DIFFICULTY = difficulty;
	}
	
	
	public void start(int time_remaining)
	{
		
	}
}
