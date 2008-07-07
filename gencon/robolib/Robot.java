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
	 * The behavioral characteristics of the robot, defined by a {@link Genotype}. 
	 */
	private final Genotype GENOTYPE;
	
	public final Master MASTER;
	
	public final short DIFFICULTY;
	
	private final HigherLevelActions ACTIONS;
	
	private final RoboUtils UTILS;
	
	
	
	public Robot(Master master)
	{
		MASTER = master;
		GENOTYPE = new Genotype(MASTER.CLIENT.getCharacterClasspath());
		DIFFICULTY = MASTER.CLIENT.getDifficulty();
		ACTIONS = new HigherLevelActions(new ActionMethods(MASTER.CLIENT));
		UTILS = new RoboUtils(this);
	}
	
	public void startTurn(int time_remaining)
	{
		ACTIONS.updateMap(MASTER.GAME_STATUS.getCurrentStatus().left);
	}
}