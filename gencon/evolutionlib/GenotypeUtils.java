package gencon.evolutionlib;

import gencon.evolutionlib.Genotype.Alleles;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

import net.thousandparsec.util.Pair;


/**
 * Methods to write/read/parse 'robot-genome' files.
 * Here are the specs for such a file:
 * 1) Nowhere is any writing (space included) permitted, but at the very bottom, below the actual 'genome'.
 * 2) Each line in the file represents the values of a trait. Lines start with ascending numbers from 0 (first line 0, then 1, ...), which represent trait types.
 * 3) After the trait-type number there is a "~"delimiter.
 * 4) Afterwards, there comes a series of numbers from 0 to 2 (inclusive), of length == {@link Genotype}.NUM_OF_TIME_RELEASE_VALUES.  
 * 
 * Here is a demo file for illustration
 * 
 * Name: TestGenome_gnm
 * -------------------------------- 
 * 0~002100110110021201201212210201
 * 1~220110102020221200122111002001
 * 2~110022220210212210220220200011
 * 3~000120010201010011121122000200
 * 4~122012011221202220211212200112
 * 5~212222202112120011200020120012
 * 6~202001221102211010212002210010
 * 7~001211011012021211212001012020
 * 8~110111011121102122201012002200
 * 9~021202202100022210100020210020
 * 10~010011020221211112021200021111
 * 11~222201121000100120222220121210
 * 12~211010202020210102120120112112
 * 
 *  ### can write anything at the bottom ###
 * -------------------------------- 
 * 
 * @author Victor Ivri
 */
public class GenotypeUtils 
{
	
	private GenotypeUtils(){}; //dummy constructor.

	/**
	 * A static method, which will create a new genome file, with random values.
	 * 
	 * @param classPath The file, where the genome will be written. 
	 */
	public static void makeRandomGenome(String classPath) throws Exception
	{
		PrintStream fileOut = makeFile(classPath);
		byte numOfAlleles = Genotype.getNumOfAlleles();
		
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
	 * A static method, which will create a new genome file, which is similar to another genome-file,
	 * but with a specified amount of random mutation in random slots. 
	 * 
	 * @param classPath The file, where the genome will be written. 
	 * @param parentClassPath The file where the 'parent' genome resides. NOTE that the file must exist.
	 * @param mutations The number of random mutations to be performed on the new genome.
	 */
	public static void makeMutantGenome(String classPath, String parentClassPath, int mutations) throws Exception
	{
		//sizing up the genome 2D matrix:
		int size_y = Genotype.getNumOfAlleles(); 
		int size_x = Genotype.NUM_OF_TIME_RELEASE_VALUES;
		
		
		// GETTING THE PARENT GENOME, AND TRANSLATING IT TO A SIMPLE MATRIX:
		
		//initializing the matrix: (will store values of alleles by row)
		byte[][] allele_matrix = new byte[size_x][size_y];
		
		//obtaining the parent genome:
		Genotype parent_genome = new Genotype(parentClassPath);
		
		//getting a byte-allele mapping:
		Map<Byte, Alleles> allele_num_mapping = Genotype.getAlleleToByteMapping();
		
		//iterating over the alleles:
		for (byte y = 0; y < size_y; y ++)
		{
			Alleles allele = allele_num_mapping.get(new Byte(y));
			List<Byte> values = parent_genome.getAlleleValues(allele);
			
			//iterating over its values, and populating the matrix:
			for (int x = 0; x < values.size(); x++)
				allele_matrix[x][y] = values.get(x);
			
		}
		//----------------
		
		// RANDOMLY GENERATING THE LOCATIONS, WHERE THE SUBSTITUTION WILL TAKE PLACE:
		//setting up a randomizer:
		Random rand = new Random(System.currentTimeMillis());
		
		//preparing to store the locations of the locations where mutation will take place:
		Collection<Pair<Integer, Integer>> mutant_locations = new HashSet<Pair<Integer,Integer>>();
		
		//generating and storing the random locations (set amount of them):
		for (int i = 0; i < mutations; i++)
			mutant_locations.add(new Pair<Integer, Integer>(rand.nextInt(size_x), rand.nextInt(size_y)));
		
		//----------------
		
		// MAKING RANDOM CHANGES IN THE RANDOMALLY SELECTED LOCATIONS:
		
		//randomly mutate the values in the chosen locations!
		for (Pair<Integer, Integer> location : mutant_locations)
		{
			byte value = allele_matrix[location.left][location.right];
			byte mutant_value = (byte)rand.nextInt(3);
			
			while (value == mutant_value) //making sure the value will be different from the original!!
			{
				mutant_value = (byte)rand.nextInt(3);
			} 
			allele_matrix[location.left][location.right] = mutant_value; // 3 because values are in range [0, 2].
		}
		//----------------
		
		
		// WRITING IT TO A FILE:

		//getting a print-stream to a new file:
		PrintStream fileOut = makeFile(classPath);
		
		for (byte y = 0; y < size_y; y++)
		{
			String line = "" + y + "~";
			for (byte x = 0; x < size_x; x++)
			{
				line += allele_matrix[x][y];
			}
			fileOut.println(line);
		}
		
	}

	
	
	/*
	 * Attempts to create the new file.
	 * Returns a PrintStream to that file.
	 */
	private static PrintStream makeFile(String classPath) throws Exception
	{
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
		int sizeOfGenum = Genotype.getNumOfAlleles();
		
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
	private static boolean checkFormat(String classPath) throws FileNotFoundException, Exception
	{
		//checks whether the filename is correct (has the "_gnm" suffix):
		if (!classPath.substring(classPath.length() - 4, classPath.length()).equals("_gnm"))
			return false;

		//proceeds to read contents:
		Scanner fileIn = new Scanner(new File(classPath));
		
		byte alleleQuantity = Genotype.getNumOfAlleles();

		for (byte i = 0; i < alleleQuantity; i++)
		{
			String line = fileIn.nextLine();
			
			//CHECKING DELIMITER:
			byte delim_pos = (byte)line.indexOf("~");
			//make sure it's legal: (2nd or 3rd)
			if (!(delim_pos == 1 || delim_pos == 2))
				return false;
			
			//CHECKING NUMBER OF TRAIT: (must equal number of line)
			byte trait_num = new Byte(line.substring(0, delim_pos)).byteValue();
			if (trait_num != i)
				return false;
			
			//CHECK LENGTH OF VALUES:
			String values = line.substring(delim_pos + 1).trim();
			
			
			if (values.length() != Genotype.NUM_OF_TIME_RELEASE_VALUES)
				return false;
			
			//CHECK THAT ALL VALUES ARE BETWEEN 0 AND 2: 
			for (int j = 0; j < values.length(); j++)
			{
				byte value = new Byte(values.substring(j, j + 1)).byteValue();
				if(!(value == 0 || value == 1 || value == 2))
					return false;
			}
		}
		
		return true;
	}

}
