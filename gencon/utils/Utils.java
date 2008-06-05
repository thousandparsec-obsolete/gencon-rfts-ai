package gencon.utils;

import java.io.PrintStream;

public class Utils 
{
	private Utils(){} //DUMMY CONSTRUCTOR : STATIC CLASS
	
	
	/**
	 * Gets the class in the specified classpath, and deals with exceptions
	 * by returning null.
	 * 
	 * @param classPath the class path leading to the class in question.
	 * @return the {@link Class} in the class path, or null if invalid.
	 */
	public static Class getClass(String classPath)
	{
		try
		{
			return Class.forName(classPath);
		}
		catch (ClassNotFoundException e)
		{
			Utils.PrintTraceIfDebug(e, true);
			return null;
		}
		
	}
	
	/**
	 * Prints exception stack trace, if verbose debug mode is on.
	 * Avoids the mess of the usual exception.printStackTrace(), 
	 * where it prints in System.err, in parallel with System.out.
	 * This way, info will remain chronologically consistent.
	 * 
	 */
	public static void PrintTraceIfDebug(Exception e, boolean verboseDebugMode)
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
