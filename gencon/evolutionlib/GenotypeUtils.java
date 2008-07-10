package gencon.evolutionlib;

import gencon.robolib.Genotype;
import gencon.robolib.Genotype.Alleles;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.StringTokenizer;

import net.thousandparsec.util.Pair;

public class GenotypeUtils 
{
	
	private GenotypeUtils(){}; //dummy constructor.

	/**
	 * A static method, which will create a genome file, with random values.
	 * 
	 * Intended for use in evolutionary framework. 
	 * 
	 * This method creates a new file in the specified location. 
	 * The end of the file will have a "_gnm" suffix attached to it.
	 * 
	 * @param classPath The file, where the genome will be written. 
	 */
	public static void makeRandomGenome(String classPath) throws Exception
	{
		PrintStream fileOut = makeFile(classPath);
		int numOfAlleles = Alleles.values().length;
		
		//prints lines, starting with the number of allele, then delimiter, then the number of time-released values.
		Random rand = new Random(System.currentTimeMillis());
		for (int i = 0; i < numOfAlleles; i++)
		{
			//prints the number of the allele, followed by the delimiter:
			fileOut.print(i + "~");
			
			String values = "";
			
			//appends random values b/w 0---2 a specified amount of times.
			for (int j = 0; j < Genotype.NUM_OF_TIME_RELEASE_VALUES; j++)
				values = values + rand.nextInt(3); 
			
			fileOut.print(values);
			//go down one line
			fileOut.print("\n"); 
		}
		
		fileOut.close();
		
	}

	/**
	 * A static method, which will create a genome file, which is similar to another genome-file,
	 * but with a specified amount of random mutation in random slots.
	 * 
	 * Intended for use in evolutionary framework. 
	 * 
	 * This method creates a new file in the specified location. 
	 * The end of the file will have a "_gnm" suffix attached to it.
	 * 
	 * @param classPath The file, where the genome will be written. 
	 * @param parentClassPath The file where the 'parent' genome resides. NOTE that the file must exist.
	 * @param mutations The number of random mutations to be performed on the new genome.
	 */
	public static void makeMutantGenome(String classPath, String parentClassPath, byte mutations) throws Exception
	{
		PrintStream fileOut = makeFile(classPath);
		
		//TO-DO 
	}

	
	
	/*
	 * Attempts to create the new file, and adds a "_gnm" suffix to it
	 */
	private static PrintStream makeFile(String classPath) throws Exception
	{
		classPath += "_gnm";
		File genFile = new File(classPath);
		
		//if it already exists, exit with exception:
		if (genFile.exists())
			throw new Exception("File with class-path: " + classPath + " already exists!");
		
		if (!genFile.createNewFile())
			throw new Exception("Cannot create file with class-path: " + classPath);
		
		return new PrintStream(new File(classPath));
	}

	
	
	public static Map<Alleles, List<Byte>> parseGenome(String classPath) throws FileNotFoundException, Exception
	{
		//check the format:
		if (!checkFormat(classPath))
			throw new Exception("Error: The genome-file with class-path: " + classPath + " has incorrect format");
		
		Scanner fileIn = new Scanner(new File(classPath));

		//how many alleles (lines):
		int sizeOfGenum = Alleles.values().length;
		
		//read and translate:
		Map<Alleles, List<Byte>> traitsMap = new HashMap<Alleles, List<Byte>>();
		for (int i = 0; i < sizeOfGenum; i++)
		{
			String line = fileIn.nextLine();
			Pair<Alleles, List<Byte>> traitsPair = parseLine(line);
			traitsMap.put(traitsPair.left, traitsPair.right);
		}
		
		return traitsMap;
	}
	
	
	/*
	 * Parses a line from the character file.
	 */
	private static Pair<Alleles, List<Byte>> parseLine(String line) throws Exception
	{
		StringTokenizer st = new StringTokenizer(line, "~");
		
		byte byte_trait = new Byte(st.nextToken());
		Alleles trait = parseTrait(byte_trait);
		
		String str_values = st.nextToken();
		List<Byte> values = new ArrayList<Byte>(str_values.length());
		for (int i = 0; i < str_values.length(); i++)
		{
			byte value = new Byte(str_values.substring(i, i + 1)).byteValue();
			values.add(value);
		}
			
		return new Pair<Alleles, List<Byte>>(trait, values);
		
	}
	
	/*
	 * THE MAPPING B/W BYTE <--> TRAITS, TO PARSE FROM THE GENOME FILE.
	 * The mapping is b/w traits, and their location in the Alleles enum.
	 */
	private static Alleles parseTrait(byte value)
	{
		Map<Byte, Alleles> mapping = Genotype.getAlleleToByteMapping();
		
		return mapping.get(new Byte(value));
	}
	
	/*
	 * Checks the specified file, to see whether or not it fits the standard format.
	 */
	private static boolean checkFormat(String classPath)
	{
		//TO DO!!
		
		//for now:
		return true;
	}

}
