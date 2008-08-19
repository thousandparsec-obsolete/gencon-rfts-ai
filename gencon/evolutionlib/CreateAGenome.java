package gencon.evolutionlib;


/**
 * A simple harness to create a random "genome" file, with a specified name, at a specified location.
 * 
 * @author Victor Ivri
 */
public class CreateAGenome 
{

	/**
	 * @param args: First argument is the file-name; second argument is the directory path.
	 */
	public static void main(String[] args) 
	{
		String name = args[0];
		String directory = args[1];
		
		String classPath = directory + "/" + name;
		
		System.out.println("Filename: " + name);
		System.out.println("Target directory: " + directory);
		System.out.println("The resulting classpath: " + classPath);
		
		try
		{
			GenotypeUtils.makeRandomGenome(classPath);
			System.out.println("Success.");
		}
		catch (Exception e)
		{
			System.out.println("Failed to create genome file. See cause below:");
			e.printStackTrace();
		}
	}

}
