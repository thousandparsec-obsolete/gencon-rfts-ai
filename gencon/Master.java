package gencon;

import java.io.PrintStream;
import java.util.Scanner;

import net.thousandparsec.netlib.SequentialConnection;
import net.thousandparsec.netlib.Visitor;

import gencon.clientLib.*;
import gencon.gamelib.FullGameStatus;
import gencon.utils.*;

/**
 * The controller class, that regulates the operation of the system as a whole.
 * This class is run by any harness, by first constructing it (with optional arguments),
 * and then using the <code>run()</code> method (possibly in a separate thread). 
 *
 * @author Victor Ivri
 */
public class Master implements Runnable
{
	//in/out
	public final static ScannerListener in = new ScannerListener(new Scanner(System.in));
	public final static PrintStream out = System.out;
	public final static String QUIT = "q";
	
	
	//maintanance
	private boolean verboseDebugMode = true; //true by default
	public final static int NORMAL_EXIT = 0;
	public final static int ABNORMAL_EXIT = -1;
	
	
	//connection related
	public final Client CLIENT;
	
	
	private FullGameStatus gameStatus;
	
	
	/**
	 * 
	 * @param args For correct argument syntax, see README.
	 */
	public Master(String[] args)
	{
		CLIENT = new Client(this);
		init(args);
	}
	
	private void init(String[] args) 
	{
		out.println("Welcome to GenCon (Genetic Conquest): An AI Client for Thousand Parsec : RFTS ruleset.");
		
		//initializing input listener.
		in.activate(this);
		out.println("To quit at any time, enter 'q', then press RETURN.");
		
		//initializing client
		try
		{
			CLIENT.runClient(args);
		}
		catch (Exception e)
		{
			exit("Failed initializing client.", ABNORMAL_EXIT, e);
		}
		
		//initializing game status
		gameStatus = new FullGameStatus(CLIENT, CLIENT.getDifficulty(), CLIENT.getPlayerName());
		
		out.println("Done initializing GenCon.");
	}
	
	/**
	 * Runs GenCon, possibly in a separate thread.
	 */
	public void run()
	{
		
		
		gameCycle();
	}
	
	private void gameCycle()
	{
		startOfTurnRoutine();
	}
	
	private void startOfTurnRoutine()
	{
		try
		{
			gameStatus.incrementTurn();
			//CLIENT.eventLogger.dumpLogStd();
		}
		catch (Exception e) /// IN REALITY, IT SHOULDN'T QUIT AT THIS POINT
		{
			exit("Unsuccessful updating game status.", ABNORMAL_EXIT, e);
		}
	}
	
	private void endOfTurnRoutine()
	{
		
	}
	
	private void startRobot()
	{
		
	}
	
	private void stopRobot()
	{
		
	}
	
	
	public void setVerboseDebugMode(boolean mode)
	{
		verboseDebugMode = mode;
	}

	/**
	 * Properly exits GenCon.
	 * 
	 * @param message To be displayed on exit.
	 * @param exitType Specifies if it was a normal, or abnormal exit (0 or otherwise).
	 * @param e The exception, which triggered the exit, if it exists.
	 */
	public void exit(String message, int exitType, Exception e)
	{
		out.println("\n______________________________________");
		out.println("Exiting GenCon.\nReason: " + message);
		
		if (e != null)
		{
			out.println("! Fatal exception ! (If Verbose Debug Mode is on, see details below)");
			Utils.PrintTraceIfDebug(e, isVerboseDebugMode());
		}
		
		try
		{
			//closing input listener
			out.print("Closing input listener... ");
			in.close();
			out.println("done.");
			
			//exiting client
			CLIENT.exit();
			
			out.println("Clean exit.");
			
			out.close();
			System.exit(NORMAL_EXIT);
		}
		catch (Exception exc)
		{
			out.println("Error on closing GenCon. Exiting anyway.");
			Utils.PrintTraceIfDebug(exc, isVerboseDebugMode());
			System.exit(ABNORMAL_EXIT);
		}
		
	}
	
	public boolean isVerboseDebugMode()
	{
		return verboseDebugMode;
	}
}
