package gencon.utils;

import java.io.EOFException;
import java.io.IOException;
import java.io.PrintStream;

import net.thousandparsec.netlib.TPException;

import gencon.clientLib.*;

public class Utils 
{
	private Utils(){} //DUMMY CONSTRUCTOR : STATIC CLASS

	
	/**
	 * Prints exception stack trace, if verbose debug mode is on.
	 * Avoids the mess of the usual exception.printStackTrace(), 
	 * where it prints in System.err, in parallel with System.out.
	 * This way, info will remain chronologically consistent.
	 * 
	 */
	public synchronized static void PrintTraceIfDebug(Exception e, boolean verboseDebugMode)
	{
		PrintStream stout = System.out;
		if (verboseDebugMode)
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
	
	
	
}
