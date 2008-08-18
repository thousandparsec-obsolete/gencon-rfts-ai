package gencon.test;

import gencon.evolutionlib.GenotypeUtils;

/*
 * THIS CLASS IS FOR EXPERIMENTATION, 
 * AND IS NOT A FORMAL TEST SUITE FOR THE CLIENT
 * 
 */


public class test {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		try
		{
			GenotypeUtils.makeRandomGenome("/home/vitya/Thousand_Parsec_Devel_Folder/gencon-rfts-ai/Risk_genome");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		/*
		String parent = "/home/vitya/Thousand_Parsec_Devel_Folder/gencon-rfts-ai/TestGenome2_gnm";
		for (int i = 1; i < 10; i++)
		{
			String classPath = "/home/vitya/Thousand_Parsec_Devel_Folder/gencon-rfts-ai/MutantGenome" + i + "_gnm";
			
			System.out.println("Parent: " + parent);
			System.out.println("Current: " + classPath);
			
			try
			{
				GenotypeUtils.makeMutantGenome(classPath, parent, 5);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
			parent = "" + classPath;
	
		}
		*/
	}

}
