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
				GenotypeUtils.makeRandomGenome("/home/vitya/Thousand_Parsec_Devel_Folder/gencon-rfts-ai/TestGenome2");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
	}

}
