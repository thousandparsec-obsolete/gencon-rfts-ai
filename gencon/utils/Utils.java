package gencon.utils;

import gencon.clientLib.Client;

import java.io.PrintStream;
import java.net.URI;

/**
 * A collector class for general utility methods.
 * 
 * @author Victor Ivri
 *
 */
public class Utils 
{
	private Utils(){} //DUMMY CONSTRUCTOR : STATIC CLASS

	
	public static PrintStream stout = System.out;
	
	/**
	 * Prints exception stack trace, if verbose debug mode is on.
	 * Avoids the mess of the usual exception.printStackTrace(), 
	 * where it prints in System.err, in parallel with System.out.
	 * This way, info will remain chronologically consistent.
	 * 
	 */
	public synchronized static void PrintTraceIfDebug(Exception e, Client client)
	{
		
		if (client.isVerboseDebugMode())
		{
			stout.println("______________________________________");
			stout.println("DEBUG:");
			stout.println("Exception in : " + e.getClass().getName());
			stout.println("Cause : " + e.getMessage());
			stout.println("Stack trace:");
			StackTraceElement[] stackTrace = e.getStackTrace();
			for (StackTraceElement ste : stackTrace)
				stout.println(ste.toString());
			
			stout.println("______________________________________");
		}
	}
	
	public synchronized static String getUsrnameFromURI(URI uri)
	{
		String[] usr = uri.getUserInfo().split(":");
		return usr[0];
	}
	

}
