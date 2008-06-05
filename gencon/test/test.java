package gencon.test;

import java.util.*;


/*
 * THIS CLASS IS FOR EXPERIMENTATION, 
 * AND IS NOT A FORMAL TEST SUITE FOR THE CLIENT
 * 
 */


public class test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	
		
		Calendar moment1 = Calendar.getInstance();
	
		try
		{
			Thread.sleep(10000);
		}
		catch (InterruptedException e)
		{
			System.out.println("interrupted.");
		}
		
		Calendar moment2 = Calendar.getInstance();
		
		System.out.println(moment1.get(Calendar.DATE) + " " + moment1.get(Calendar.HOUR) + ":" 
				+ moment1.get(Calendar.MINUTE) + ":" + moment1.get(Calendar.SECOND));
		
		System.out.println(moment2.get(Calendar.DATE) + " " + moment2.get(Calendar.HOUR) + ":" 
				+ moment2.get(Calendar.MINUTE) + ":" + moment2.get(Calendar.SECOND));
		
		
		/*
		LinkedList<Integer> list = new LinkedList<Integer>();
		
		for (int i = 0; i < 11; i++)
			list.add(new Integer(i));
		
		System.out.println(list.element().toString());
		System.out.println(list.getFirst().toString());
		System.out.println(list.getFirst().toString());
		System.out.println(list.getLast().toString());
		System.out.println(list.getLast().toString());
		
		
		
		for (int i = 0; i < 100; i++)
		{
			System.err.println(i);
			
			
			try
			{
				if (i == 50)
				{
					throw new Exception();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		*/
	}

}
