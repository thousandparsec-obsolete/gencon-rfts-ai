package gencon.robolib;

import gencon.robolib.Genotype.Alleles;

import java.util.Map;

/**
 * The -actual- behavioral characteristics of the robot, dependant on its {@link Genotype}.
 * 
 * @author Victor Ivri
 *
 */
public class Phenotype 
{
	private final Genotype GENOME;
	private Map<Alleles, Byte> currentTraits;
	
	Phenotype(Genotype genome)
	{
		GENOME = genome;
	}
	
	void updatePhenotype(byte turn_num)
	{
		currentTraits = GENOME.getGenome(turn_num);
	}
	
	
	
	
}
