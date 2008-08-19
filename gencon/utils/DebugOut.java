package gencon.utils;

import java.io.PrintStream;

/**
 * A class, which generates output to a certain {@link PrintStream},
 * depending on the state of a flag, set in the constructor. Used for debug output.
 * 
 * @author Victor Ivri
 */
public class DebugOut 
{
	private boolean verbose_mode;
	public final PrintStream PS;
	
	public DebugOut(PrintStream ps)
	{
		verbose_mode = true; //by default!
		PS = ps;
	}
	
	public void pl(String st)
	{
		if (verbose_mode)
			PS.println(st);
	}
	
	public void pr(String st)
	{
		if (verbose_mode)
			PS.print(st);
	}
	
	public boolean getVerboseMode()
	{
		return verbose_mode;
	}
	
	public void setVerboseMode(boolean flag)
	{
		verbose_mode = flag;
	}
}
