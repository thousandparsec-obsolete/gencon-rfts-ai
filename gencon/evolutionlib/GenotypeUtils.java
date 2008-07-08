package gencon.evolutionlib;

import gencon.robolib.Genotype;
import gencon.robolib.Genotype.Alleles;

import java.util.List;
import java.util.Map;

import net.thousandparsec.util.Pair;

public class GenotypeUtils 
{
	
	private GenotypeUtils(){}; //dummy constructor.

	/**
	 * A static method, which will create a genome file, with random values.
	 * Intended for use in evolutionary framework. 
	 * This method creates a new file in the specified location. 
	 * The end of the file will have a "_gnm" suffix attached to it.
	 * 
	 * @param name A unique name, to identify the genome in the file. 
	 * @param classPath The file, where the genome will be written. NOTE that the file must exist.
	 */
	public static void makeRandomGenome(String uniqueName, String classPath)
	{
		
	}

	/**
	 * A static method, which will create a genome file, which is similar to another genome file,
	 * but with a specified amount of random mutation.
	 * Intended for use in evolutionary framework. 
	 * This method creates a new file in the specified location. 
	 * The end of the file will have a "_gnm" suffix attached to it.
	 * 
	 * @param name A unique name, to identify the genome in the file. 
	 * @param classPath The file, where the genome will be written. NOTE that the file must exist.
	 * @param parent The base {@link Genotype}, on which the new one will be based.
	 * @param mutations The number of random mutations to be performed on the new genome.
	 */
	public static void makeMutantGenome(String uniqueName, String classPath, Genotype parent, byte mutations)
	{
		
	}

	/**
	 * Checks the the specified file, to see whether or not it fits the standard format.
	 */
	public static boolean checkFormat(String classPath)
	{
		/*
		 * go line-by-line to check the format.
		 */
	}

	public static Map<Alleles, List<Byte>> parseGenum(String uniqueName, String classPath)
	{
		
	}

	/*
	 * Parses a line from the character file.
	 */
	private static Pair<Alleles, List<Byte>> parseLine(String line)
	{
		
	}
	
	/*
	 * THE MAPPING B/W SHORT <--> TRAITS, TO PARSE FROM THE GENOME FILE.
	 */
	private static Alleles parseTrait(Short value)
	{
		switch (value)
		{
		/*
		 * ALL CASES OF THE MAPPING WILL BE CONTAINED HERE!
		 */
		}
	}

}
