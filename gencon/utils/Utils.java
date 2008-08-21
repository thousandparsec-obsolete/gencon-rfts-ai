package gencon.utils;

import gencon.Master.RULESET;
import gencon.clientLib.Client;

import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

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
	 * Parses the arguments at input. See README for proper syntax.
	 * 
	 * @param args The arguments given by the {@link Client}.
	 * 
	 * @return {@link List}<{@link Object}>, in the following order:
	 * 1) {@link Client.RULESET} : The type of game to be played.
	 * 2) {@link String} : The URI string.
	 * 3) {@link String} : The genome file classpath.
	 * 4) {@link Byte} : The difficulty of the AI.
	 * 5) {@link Boolean} : Verbose debug mode on/off.
	 * 
	 * @throws IllegalArgumentException if there's an error in the arguments.
	 */
	public synchronized static List<Object> parseArgs(String[] args) throws IllegalArgumentException
	{
		//checking format!
		try
		{
			assert args.length == 5;
			assert args[0].equals("rfts") || args[0].equals("risk");
			assert args[3].matches("[1-9]");
			assert args[4].equals("d") || args[4].equals("n"); //d - debug ; n - normal.
		}
		catch (Error e)
		{
			throw new IllegalArgumentException("Illegal Arguments. See documentation for proper syntax. Try again.");
		}           
		
		//the variables to be returned: (some set to default)
		RULESET ruleset = RULESET.RISK;
		String URIstr = args[1];
		String genomeClassPath = args[2];
		Short difficulty = new Short(args[3]); 
		Boolean verboseDebugMode = true;
		
		//setting the rest based on results:
		//---------------------------------
		if (args[0].equals("rfts"))
			ruleset = RULESET.RFTS;
		//else: the default.
		
		if (args[4].equals("n"))
			verboseDebugMode = false;
		//else: the default.
		
		//---------------------------
		
		
		//composing the return:
		List<Object> returned = new ArrayList<Object>();
		returned.add(ruleset);
		returned.add(URIstr);
		returned.add(genomeClassPath);
		returned.add(difficulty);
		returned.add(verboseDebugMode);
		
		return returned;
	}
	
	/**
	 * Prints exception stack trace, if verbose debug mode is on.
	 * Avoids the mess of the usual exception.printStackTrace(), 
	 * where it prints in System.err, in parallel with System.out.
	 * This way, info on the screen will remain chronologically consistent.
	 * 
	 */
	public synchronized static void PrintTraceIfDebug(Exception e, boolean debug)
	{
		
		if (debug)
		{
			stout.println("\n______________________________________");
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
