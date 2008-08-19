package gencon.robolib;

import java.util.HashMap;
import java.util.Map;

import gencon.evolutionlib.Genotype;
import gencon.evolutionlib.Genotype.Alleles;

/**
 * The AI robot, which will play the game.
 * 
 * @author Victor Ivri
 * 
 */
public abstract class Robot 
{
	public final Genotype GENOME;

	
	private Map<Alleles, Byte> currentTraits;
	
	public Robot(Genotype genome)
	{
		GENOME = genome;
	}
	
	/**
	 * Serves as the 'internal clock' of the {@link Robot}.
	 * 
	 * NOTE: Should put a 'super.startTurn(int time_remaining)' at the beginning
	 * 			of the overriden method.
	 * 
	 * @param seconds The amount of seconds left to make a move.
	 */
	public void startTurn(int time_remaining, int turn_num)
	{
		updateTraits(turn_num);
	}
	
	
	/**
	 * Gets the relevant 'time-released' traits.
	 * 
	 * @param turn_num The subjective turn number.
	 */
	public void updateTraits(int turn_num)
	{
		currentTraits = GENOME.getGenome(turn_num);
	}
	
	public Map<Alleles, Byte> getCurrentTraits()
	{
		return new HashMap<Alleles, Byte>(currentTraits);
	}
}
