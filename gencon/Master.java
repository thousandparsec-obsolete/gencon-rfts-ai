package gencon;

import java.io.PrintStream;
import java.util.Scanner;

import net.thousandparsec.netlib.SequentialConnection;
import net.thousandparsec.netlib.Visitor;

import gencon.clientLib.*;
import gencon.gamelib.FullGameStatus;
import gencon.robolib.Robot;
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
	/**
	 * All standard input has to come from {@link Master}.in.
	 */
	public final static ScannerListener in = new ScannerListener(new Scanner(System.in));
	public final static String QUIT = "q";
	
	
	//maintanance
	private boolean verboseDebugMode = true; //true by default
	public final static int NORMAL_EXIT = 0;
	public final static int ABNORMAL_EXIT = -1;
	private final byte WORK_TIME = 10; //The time, in seconds, required to complete a turn. 
	//if remaining time in some turn is less than that, robot will not execute until next turn.
	//ONLY AN ESTIMATE NOW!!! NEED TO CALCULATE ACTUAL TIMES! (MAY DEPEND ON PING).

	
	//connection related
	public final Client CLIENT;
	
	//game-related
	public final FullGameStatus GAME_STATUS;
	private Robot robot;
	
	
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
		GAME_STATUS = new FullGameStatus(this);
		init(args);
	}
	
	private void init(String[] args) 
	{
		pl("Welcome to GenCon (Genetic Conquest): An AI Client for Thousand Parsec : RFTS ruleset.");
		
		//initializing input listener.
		in.activate(this);
		pl("To quit at any time, enter 'q', then press RETURN.");
		
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
		GAME_STATUS.init();
		
		//initializing robot:
		try
		{
			robot = new Robot(this); 
		}
		catch (Exception e)
		{
			exit("Failed initializing ai-bot.", ABNORMAL_EXIT, e);
		}
		
		pl("Done initializing GenCon.");
	}
	
	/**
	 * Runs GenCon, possibly in a separate thread.
	 */
	public void run()
	{
		delayUntilReady(); //if necessary, delay operation until GenCon can fully execute!
		gameCycle();
	}
	
	private void gameCycle()
	{
		
		//UNCOMMENT! :
		//while (true)  //if the 'exit' method is invoked at any point, the program will be killed on its own.
		//{
			
			startOfTurnRoutine();
			try
			{
				startRobot(CLIENT.getTimeRemaining() - 3); 
				//robot has the knowledge of how long to act 
				//(time remaining, minus 3 seconds for confidence).
			}
			catch (Exception e)
			{
				exit("Could not fetch time from server while initializing robot.", ABNORMAL_EXIT, e);
			}
			
		//}
	}
	
	
	/*
	 * DELAYS START OF NEW TURN, UNTIL GENCON HAS ENOUGH TIME TO OPERATE.
	 */
	private void delayUntilReady()
	{
		int timeRemaining = 0;
		
		//retreive time from client: (should be successful!)
		try 
		{
			timeRemaining = CLIENT.getTimeRemaining();
		} 
		catch (Exception e)
		{
			exit("Permanently failed to fetch time", ABNORMAL_EXIT, e);
		}
		
		
		long timeDiff = (timeRemaining - WORK_TIME) * 1000; //the difference between time remaining, and time necessary to operate, in milliseconds.
		
		if (timeDiff <= 0) //if I indeed need to wait!!!
		{
			pl("Waiting until next turn to start operation... Time remaining : " + timeRemaining + " seconds.");
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
			int time = CLIENT.getTimeRemaining();
			pl("Start of turn routine commencing... " + time + " seconds to end of turn.");
			GAME_STATUS.incrementTurn();
			//CLIENT.eventLogger.dumpLogStd();
		}
		catch (Exception e) /// IN REALITY, IT SHOULDN'T QUIT AT THIS POINT
		{
			exit("Unsuccessful updating game status.", ABNORMAL_EXIT, e);
		}
	}
	

	
	private void startRobot(int seconds_remaining)
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
		System.out.println("\n______________________________________");
		System.out.println("Exiting GenCon.\nReason: " + message);
		
		if (e != null)
		{
			System.out.println("! Fatal exception ! (If Verbose Debug Mode is on, see details below)");
			Utils.PrintTraceIfDebug(e, isVerboseDebugMode());
		}
		
		try
		{
			//closing input listener
			System.out.print("Closing input listener... ");
			in.close();
			System.out.println("done.");
			
			//exiting client
			CLIENT.exit();
			
			System.out.println("Clean exit.");
			
			System.out.close();
			System.exit(NORMAL_EXIT);
		}
		catch (Exception exc)
		{
			System.out.println("Error on closing GenCon. Exiting anyway.");
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
	
	
	/**
	 * Use this method to substitute for {@link System}.out.println(). 
	 * It prints to standard-out only if verbose-debug mode is on.
	 */
	public void pl(String st)
	{
		if (isVerboseDebugMode())
			System.out.println(st);
	}
	
	/**
	 * Use this method to substitute for {@link System}.out.print(). 
	 * It prints to standard-out only if verbose-debug mode is on.
	 */
	public void pr(String st)
	{
		if (isVerboseDebugMode())
			System.out.print(st);
	}
}
