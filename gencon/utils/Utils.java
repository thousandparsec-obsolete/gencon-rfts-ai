package gencon.utils;

import gencon.Master;
import gencon.clientLib.Client;

import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;

import net.thousandparsec.util.Pair;

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
	 * Parses the arguments at input. See documentation for proper syntax.
	 * 
	 * @param args The arguments given by the {@link Client}.
	 * 
	 * @return Pair<Short, Pair<String, String>>, where the first pair is difficulty,  
	 * and the next pair is <genome_name, genome_classpath>.
	 */
	public synchronized static Pair<Short, Pair<String, String>> parseArgs(String[] args) 
	{
		//the variables to be returned.
		short difficulty = 5; //the default difficulty
		String URIstr = "";
		String genomeClassPath = "";
		///////////
		
		try
		{
			if (args.length == 0)  {/*no arguments; normal operation*/}
			
			//configuring options for autorun:
			//must have 4 arguments, and first one must be '-a'.
			else if (args.length == 4 && args[0].equals("-a")) 
			{
				difficulty = new Short(args[1]).shortValue(); //must be a short; otherwise, will throw exception.				
				//make sure difficulty in range:
				if (difficulty <= 0 || difficulty >= 10)
					throw new Exception();
				
				URIstr = args[2];
				
				genomeClassPath = args[3];
			}
			//for any other input:
			else
				throw new Exception();
				
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Illegal Arguments. See documentation for proper syntax. Try again.");
		}
		
		//composing the return:
		Pair<String, String> rightPair = new Pair<String, String>(URIstr, genomeClassPath);
		Pair<Short, Pair<String, String>> pair = new Pair<Short, Pair<String,String>>(difficulty, rightPair);
		return pair; 
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
	
	public synchronized static boolean setVerboseDebug()
	{
		boolean vdm = true; //returned

		boolean ok = true; //for the loop
		do {
			stout.print("Verbose debug mode? (y / n) : ");
			String in = Master.in.next();
			if (in.equals("y"))
			{
				stout.println("Verbose debug mode set to: true");
				vdm = true;
				ok = true;
			}
			else if (in.equals("n"))
			{
				stout.println("Verbose debug mode set to: false");
				vdm = false;
				ok = true;
			}
			else
			{
				stout.println("Please enter 'y' or 'n'.");
				ok = false;
			}
		} while (!ok);
		
		return vdm;
	}
	
	/** 
	 * set the URI by standard user input.
	 */
	public synchronized static URI manualSetURI()
	{
		URI uri = null;
		boolean ok = false;
		do {
	 		stout.print("Enter the URI of the server (without user info): ");
			String URIString = Master.in.next();
			uri = setURI(URIString);

		} while (uri == null);
		
		return uri;
	}
	
	/**
	 * Requests the classpath for the character-file from the user.
	 * 
	 * @return The classpath.
	 */
	public synchronized static String manualSetGenomeClasspath()
	{
		String cp = "";
		boolean ok = false;
		
		do
		{
			stout.print("Enter the classpath of the genotype-file you wish to access: ");
			cp = Master.in.next();
			
			if (!cp.equals(""))
				ok = true;
			else
			{
				ok = false;
				System.out.println("Invalid input: Cannot be empty. Try again.");
			}
			
		} while (!ok);
		
		return cp;
	}
	
	
	/**
	 * Making the server URI from a string.
	 * Retrurns a {@link URI} if successful, <code>null</code> otherwise.
	 */
	public synchronized static URI setURI(String URIString)
	{
		URI uri = null;
		try
		{
			uri = new URI(URIString);
		}
		catch (URISyntaxException e)
		{
			stout.println("Error setting URI: " + e.getMessage());
		}
		return uri;
	} 
	
	/**
	 * Sets the difficulty of the AI opponent from standard input. 
	 * @return The difficulty in the interval [1, 9].
	 */
	public synchronized static short setDifficulty()
	{
		short diff = -1;
		boolean ok = false;
		do 
		{
			stout.print("Set difficulty of AI player (1 to 9) : ");
			
			diff = new Short(Master.in.next()).shortValue();
			if (diff > 0 && diff < 10)
				ok = true;
			else
			{
				stout.println("Invalid input. Enter a number between 1 to 9. Try again.");
				ok = false;
			}
		} while (!ok);
		
		stout.println("Difficulty set to : " + diff);
		return diff;
	}

	/**
	 * Simple method that queries for user details.
	 * Returns String[], where the first index holds username, and the second holds password.
	 * TO DO: Make password entry invisible on screen!!!
	 */
	public synchronized static String[] enterUserDetails()
	{
		String[] userDetails = new String[2];
		
		stout.print("Enter username: ");
		userDetails[0] = Master.in.next();
		
		stout.print("Enter password: ");
		userDetails[1] = Master.in.next();
		
		return userDetails;
	}
	
}
