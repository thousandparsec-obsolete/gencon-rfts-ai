package gencon.robolib;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.thousandparsec.util.Pair;

public class Genotype 
{	
	/**
	 * The types of traits a {@link Genotype} has (Note that it has them necessarily).
	 *
	 */
	public static enum Traits
	{
		/*
		 * TO BE DONE! 
		 */
	}
	
	
	/*
	 * The map of the characteristics, in their corresponding place.
	 */
	private Map<Traits, Float> characteristics; 
	
	/**
	 * Constructs the class, and initializes the behavioral characteristics from the specified file.
	 * @param classPath The path of the file.
	 */
	public Genotype(String classPath)
	{
		characteristics = new HashMap<Traits, Float>();
		init(classPath);
	}
	
	
	private void init(String classPath)
	{
		/*
		 * 1) Check the format of the file
		 * 2) Go over the file, line by line:
		 * 		2-b) Parse each line, and fill the corresponding place in the map.
		 */
	}
	
	/**
	 * @return A deep copy of the full list of behavioral characteristics of this {@link Genotype}. 
	 */
	public Map<Traits, Float> getCharacteristics()
	{
		HashMap<Traits, Float> map = new HashMap<Traits, Float>();
		Set<Traits> keyset = characteristics.keySet();
		
		//deep-copy babies..
		for (Traits key : keyset)
			map.put(key, new Float(characteristics.get(key).floatValue()));
		
		return map;
	}
	
	/**
	 * Get the value of the specific {@link Traits} trait.
	 */
	public float getTraitValue(Traits trait)
	{
		return characteristics.get(trait).floatValue();
	}
	
	/*
	 * THE MAPPING B/W SHORT <--> TRAITS
	 */
	private Traits parseTrait(Short num)
	{
		switch (num)
		{
		/*
		 * ALL CASES OF THE MAPPING WILL BE CONTAINED HERE!
		 */
		}
	}
	
	/*
	 * Parses a line from the character file.
	 */
	private static Pair<Traits, Float> parseLine(String line)
	{
		
	}
	
	/*
	 * Checks the the specified file, to see whether or not it fits the standard format.
	 * The 'template' parameter is true if the file is only a template,
	 * and false if it's filled with values.
	 */
	private static boolean checkFormatOfFile(String classPath, boolean template)
	{
		/*
		 * go line-by-line to check the format.
		 */
	}
	
	/**
	 * A static method, which will create a template for the character file. 
	 * This method creates a new file in the specified location. 
	 * The end of the file will have a "_ch" suffix attached to it.
	 * 
	 * @param name The file name. 
	 * @param classPath The location of the file.
	 * @return true if successful, false otherwise.
	 */
	public static boolean makeTemplateFile(String name, String classPath)
	{
		/*
		 * 1) make the file
		 * 2) double-check the format
		 */
		
	}
	
	


}
