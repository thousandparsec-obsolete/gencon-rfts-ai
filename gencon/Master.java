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
	
	//game-related
	private FullGameStatus gameStatus;
	private final byte WORK_TIME = 7; //The time, in seconds, required to complete a turn. 
						//if remaining time in some turn is less than that, robot will not execute.
						//ONLY AN ESTIMATE NOW!!! NEED TO CALCULATE ACTUAL TIMES! (MAY DEPEND ON PING).
	private boolean quit = false; //the flag that tells gameCycle to quit!
	
	
	/**
	 * 
	 * @param args Optional argument: '-a serverURI $' 
	 * To autorun client, supply argument '-a', followed by the 'serverURI' and by game difficulty '$', 
	 * which should be replaced by any number 1 to 9. If no game difficulty provided, default is 5.
	 * The serverURI must include user info for autologin, e.g.: "tp://guest:guest@thousandparsec.net/tp".
	 * In this case, verbose debug mode will be automatically on.
	 * 
	 * If no argument provided, client will start in 'normal' mode; that is, it will rely on standard user input. 
	 *
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
		gameStatus = new FullGameStatus(CLIENT, CLIENT.getPlayerName());
		
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
		
		//UNCOMMENT! :
		//while (!quit)
		//{
			delayUntilReady(); //delay operation until GenCon can fully execute!
			startOfTurnRoutine();
		//}
	}
	
	
	/*
	 * DELAYS START OF NEW TURN, UNTIL GENCON HAS ENOUGH TIME TO OPERATE.
	 */
	private void delayUntilReady()
	{
		int timeRemaining = 0;
		
		//retreive time from client: (should be successful!)
		boolean ok = false;
		byte counter = 0;
		do 
		{
			try 
			{
				timeRemaining = CLIENT.getTimeRemaining();
				ok = true;
				counter ++;
			} 
			catch (Exception e)
			{
				counter ++;
				if (counter > 20) //if try to do it over 20 times, quit!!
					exit("Permanently failed to fetch time", ABNORMAL_EXIT, e);
			}
		} while (!ok);
		
		
		long timeDiff = (timeRemaining - WORK_TIME) * 1000; //the difference between time remaining, and time necessary to operate, in milliseconds.
		
		if (timeDiff <= 0) //if I indeed need to wait!!!
		{
			out.println("Waiting until next turn to start operation... Time remaining : " + timeRemaining + " seconds.");
			try
			{
				Thread.sleep((timeRemaining * 1000) + 1000); //wait until next turn + 1 second.
			}
			catch (InterruptedException ie)
			{
				exit("Thread interrupted!", ABNORMAL_EXIT, ie);
			}
		}
		
		
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
		
		//telling gameCycle that it's time to go home! (if it hasn't shut down already...)
		quit = true;
		
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

	public void setVerboseDebugMode(boolean mode)
	{
		verboseDebugMode = mode;
	}
}
