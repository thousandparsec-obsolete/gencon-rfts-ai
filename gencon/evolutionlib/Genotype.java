package gencon.evolutionlib;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A set of characteristics the robot has. 
 * 
 * 
 * @author Victor Ivri
 *
 */
public class Genotype 
{	
	/**
	 * The types of traits a {@link Genotype} has (Note that it has them necessarily).
	 *
	 * Each trait has a numeric value of 0, 1 or 2. 
	 * Each trait gives it's own meaning to the value.
	 * 
	 * Here's a complete rundown on the traits and their meaning:
	 * 
		////////////
		// "A_" Action behaviors:
		////////////
		
		A_COLONIALIST1, // (0) colonize own sector first ---- (1) colonize own and neighbouring non-hostile sectors --- (2) colonize own and not-neighbouring non-hostile sectors.
		A_COLONIALIST2, // (0) send colonizer without defensive fleet ---- (1) send colonizer with weak defensive fleet --- (2) send colonizer with strong defensive fleet.
		
		A_ATTACK1, // (0) send army to bordering hostile sectors ---- (1) no preference ---- (2) send army to enemy heartland.
		A_ATTACK2, // (0) send at least evenly matched army to hostile sectors --- (1) no preference ---- (2) only send huge flotillas to crush enemy.
		
		////////////
		// "E_" Economic behaviors:
		////////////
		
		E_OVERALL, // (0) prefer to develop economics over army ---- (1) no preference ---- (2) prefer to emass forces instead of developing economics.
		
		E_COLONIALIST1, // (0) produce few colonists/transports ---- (1) produce an intermediate quantity ---- (2) produce large quantity.
		E_COLONIALIST2, // (0) prefer to produce colonialist fleet in Periphery sectors ---- (1) no preference --- (2) prefer to produce colonialist fleet in Stronghold sectors
		E_COLONIALIST3, // (0) prefer to produce colonialist fleet in non-threatened sectors ---- (1) no preference ---- (2) prefern to produce colonialist fleet in threatened sectors.
		
		//the "develop economics" behaviors:
		E_INDUSTRY, // (0) industry unimportant ---- (1) normal importance ---- (2) very important.
		E_SOCENV, // (0) social-environment unimportant ---- (1) normal importance ---- (2) very important.
		E_PLANENV, // (0) planetary-environment unimportant ---- (1) normal importance ---- (2) very important.
		E_POPMAINT, // (0) population-maintanance unimportant ---- (1) normal importance ---- (2) very important.
		
		E_RESEARCH, // (0) ship-tech unimportant ---- (1) normal importance ---- (2) very important.
	 * 
	 * 
	 */
	public static enum Alleles
	{
		////////////
		// "A_" Action behaviors:
		////////////
		
		A_COLONIALIST1, // (0) colonize own sector first ---- (1) colonize own and neighbouring non-hostile sectors --- (2) colonize own and not-neighbouring non-hostile sectors.
		A_COLONIALIST2, // (0) send colonizer without defensive fleet ---- (1) send colonizer with weak defensive fleet --- (2) send colonizer with strong defensive fleet.
		
		A_ATTACK1, // (0) send army to bordering hostile sectors ---- (1) no preference ---- (2) send army to enemy heartland.
		A_ATTACK2, // (0) send at least evenly matched army to hostile sectors --- (1) no preference ---- (2) only send huge flotillas to crush enemy.
		
		////////////
		// "E_" Economic behaviors:
		////////////
		
		E_OVERALL, // (0) prefer to develop economics over army ---- (1) no preference ---- (2) prefer to emass forces instead of developing economics.
		
		E_COLONIALIST1, // (0) produce few colonists/transports ---- (1) produce an intermediate quantity ---- (2) produce large quantity.
		E_COLONIALIST2, // (0) prefer to produce colonialist fleet in Periphery sectors ---- (1) no preference --- (2) prefer to produce colonialist fleet in Stronghold sectors
		E_COLONIALIST3, // (0) prefer to produce colonialist fleet in non-threatened sectors ---- (1) no preference ---- (2) prefern to produce colonialist fleet in threatened sectors.
		
		//the "develop economics" behaviors:
		E_INDUSTRY, // (0) industry unimportant ---- (1) normal importance ---- (2) very important.
		E_SOCENV, // (0) social-environment unimportant ---- (1) normal importance ---- (2) very important.
		E_PLANENV, // (0) planetary-environment unimportant ---- (1) normal importance ---- (2) very important.
		E_POPMAINT, // (0) population-maintanance unimportant ---- (1) normal importance ---- (2) very important.
		
		E_RESEARCH, // (0) ship-tech unimportant ---- (1) normal importance ---- (2) very important.
	}
	
	
	/**
	 * The map of the characteristics, in their corresponding place.
	 * The list of values represents the time-release mechanism.
	 */
	final Map<Alleles, List<Byte>> GENOME; 
	
	
	/**
	 * The number of turns it takes to switch to the next value of the {@link Alleles} trait, in the 'time-release' {@link List} of the GENOME.
	 */
	public static byte TIME_RELEASE = 10; 
	
	/**
	 * The number of 'time-released' values each {@link Alleles} trait will have.
	 */
	public final static byte NUM_OF_TIME_RELEASE_VALUES = 16;
	
	
	
	/**
	 * Constructs the class, and initializes the genotype from the specified file, and unique name.
	 * @param uniqueName The unique name of the genome to be used in the robot.
	 * @param classPath The path of the file.
	 */
	public Genotype(String classPath) throws Exception
	{
		GENOME = GenotypeUtils.parseGenome(classPath);
		//test();
	}
	
	/**
	 * Provides a mapping of each {@link Alleles} trait, and its {@link Byte} value,
	 * with respect to the current turn number. 
	 * 
	 * Recall that the actual 'genome' code for each {@link Alleles} is a list of time-released values, 
	 * and this method returns the relevant value relative to the 'age' of the robot. 
	 * 
	 * @param turn_num The current turn number.
	 */
	public Map<Alleles, Byte> getGenome(int turn_num)
	{
		//determine the spot in the time-release list of values:
		int spot = (turn_num - 1) / TIME_RELEASE;
		
		Map<Alleles, Byte> map = new HashMap<Alleles, Byte>();
		Set<Alleles> keyset = GENOME.keySet();
		
		for (Alleles key : keyset)
			map.put(key, GENOME.get(key).get(spot));
		
		return map;
	}
	
	/**
	 * Get the value of the specific {@link Alleles} trait.
	 */
	public List<Byte> getAlleleValues(Alleles allele)
	{
		return new ArrayList(GENOME.get(allele));
	}
	
	
	/**
	 * A method to determine the quantity of traits, and the mapping between actual traits and numeric values.
	 * This assumes that the trait gets its numeric value from its place in the enum. 
	 * @return
	 */
	public synchronized static Map<Byte, Alleles> getAlleleToByteMapping()
	{
		Alleles[] traits = Alleles.values();
		
		Map<Byte, Alleles> mapping = new HashMap<Byte, Alleles>();
		
		for (byte i = 0; i < traits.length; i++)
			mapping.put(new Byte(i), traits[i]);
		
		return mapping;
	}
	
	public synchronized static byte getNumOfAlleles()
	{
		return (byte)Alleles.values().length;
	}

	private void test()
	{
		Set<Alleles> keyset = GENOME.keySet();
		
		for (Alleles al : keyset)
		{
			System.out.print(al + " ~ ");
			List<Byte> values = GENOME.get(al);
			for (Byte b : values)
				System.out.print(b.byteValue());
			
			System.out.print("\n");
		}
		
		//160 is the maximum amount of turns in RFTS:
		for (byte i = 1; i <= 160; i += 10)
		{
			System.out.print("\nTurn " + i + " :  ");
			Map<Alleles, Byte> gnm = getGenome(i);
			
			Set<Alleles> ks2 = gnm.keySet();
			
			for (Alleles a : ks2)
				System.out.print(" (" + a + " : " + gnm.get(a) +") ");
		}
		
		//corner case:
		byte i = (byte)160;
		System.out.print("\nTurn " + i + " :  ");
		Map<Alleles, Byte> gnm = getGenome(i);
		
		Set<Alleles> ks2 = gnm.keySet();
		
		for (Alleles a : ks2)
			System.out.print(" (" + a + " : " + gnm.get(a) +") ");
	}
}
