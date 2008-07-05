package gencon.robolib;

import gencon.Master;
import gencon.clientLib.Client;
import gencon.gamelib.FullGameStatus;

/**
 * The AI robot, which will play the game.
 * 
 * @author Victor Ivri
 * 
 */
public class Robot 
{
	/*
	 * The behavioral characteristics of the robot, defined by a {@link Character}. 
	 */
	private final Character CHARACTER;
	
	public final Master MASTER;
	
	public final short DIFFICULTY;
	
	private final HigherLevelActions ACTIONS;
	
	
	
	public Robot(Master master)
	{
		MASTER = master;
		CHARACTER = new Character(MASTER.CLIENT.getCharacterClasspath());
		DIFFICULTY = MASTER.CLIENT.getDifficulty();
		ACTIONS = new HigherLevelActions(new ActionMethods(MASTER.CLIENT));
	}
	
	public void startTurn(int time_remaining)
	{
		ACTIONS.updateMap(MASTER.GAME_STATUS.getCurrentStatus().left);
	}
}
