package gencon;

import java.util.*;

public class test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	
		LinkedList<Integer> list = new LinkedList<Integer>();
		
		for (int i = 0; i < 11; i++)
			list.add(new Integer(i));
		
		System.out.println(list.element().toString());
		System.out.println(list.getFirst().toString());
		System.out.println(list.getFirst().toString());
		System.out.println(list.getLast().toString());
		System.out.println(list.getLast().toString());
	}

}
