package gencon.utils;

import gencon.Master;
import gencon.clientLib.Client;

import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * A collector class for general utility methods.
 * 
 * @author Victor Ivri
 *
 */
public class Utils 
{
	private Utils(){} //DUMMY CONSTRUCTOR : STATIC CLASS

	/**
	 * Initialize this class.
	 *
	 */
	public void init()
	{
		
	}
	
	public static PrintStream stout = System.out;
	
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
		System.out.println(uri.getUserInfo());
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
	
	/* 
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
