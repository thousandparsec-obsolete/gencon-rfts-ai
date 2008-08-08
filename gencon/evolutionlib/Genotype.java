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
	 * The types of traits a {@link Genotype} has. A genotype may have a varying am
	 *
	 * Each trait should have a numeric value (or list thereof) in range [0, 2] attached to it in a {@link Map}.
	 * Each trait gives it's own meaning to the value.
	 * 
	 */
	public static enum Alleles
	{
		ALLELE_1, ALLELE_2, ALLELE_3, ALLELE_4, ALLELE_5, ALLELE_6, ALLELE_7, ALLELE_8, ALLELE_9, ALLELE_10;
	}
	
	
	/**
	 * The map of the characteristics, in their corresponding place.
	 * The list of values represents the time-release mechanism.
	 */
	final Map<Alleles, List<Byte>> GENOME; 
	
	
	/**
	 * The number of turns it takes to switch to the next value of the {@link Alleles} trait, in the 'time-release' {@link List} of the GENOME.
	 */
	public static byte TIME_RELEASE = 7; 
	
	/**
	 * The number of 'time-released' values each {@link Alleles} trait will have.
	 */
	public final static byte NUM_OF_TIME_RELEASE_VALUES = 20;
	
	
	
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
	 * with respect to the current turn number. The genome 'wraps around', 
	 * in case the turn number becomes greater than the number of unique values specified by:
	 * TIME_RELEASE * NUM_OF_TIME_RELEASE_VALUES .
	 * 
	 * Recall that the actual 'genome' code for each {@link Alleles} is a list of time-released values, 
	 * and this method returns the relevant value relative to the 'age' of the robot. 
	 * 
	 * @param turn_num The current turn number.
	 */
	public Map<Alleles, Byte> getGenome(int turn_num)
	{
		//wrap-around if needed:
		turn_num = (TIME_RELEASE * NUM_OF_TIME_RELEASE_VALUES) % turn_num;
		
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
		return new ArrayList<Byte>(GENOME.get(allele));
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
