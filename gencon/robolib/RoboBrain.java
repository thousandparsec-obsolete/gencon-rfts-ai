package gencon.robolib;

import gencon.Master;
import gencon.evolutionlib.Genotype;
import gencon.evolutionlib.Genotype.Alleles;
import gencon.robolib.RFTS.HigherLevelActions;

import java.util.HashMap;
import java.util.Map;

public abstract class RoboBrain 
{
	private final Genotype GENOME;
	private Map<Alleles, Byte> currentTraits;
	
	public RoboBrain(Genotype genome)
	{
		GENOME = genome;
	}
	
	public void updateTraits(int turn_num)
	{
		currentTraits = GENOME.getGenome(turn_num);
	}
	
	public Map<Alleles, Byte> getCurrentTraits()
	{
		return new HashMap<Alleles, Byte>(currentTraits);
	}
}
