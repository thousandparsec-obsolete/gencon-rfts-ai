package gencon;

import java.util.Scanner;

import gencon.clientLib.*;
import gencon.evolutionlib.Genotype;
import gencon.gamelib.FullGameStatus;
import gencon.gamelib.RFTS.FullGameStatusRFTS;
import gencon.gamelib.RISK.FullGameStatusRISK;
import gencon.robolib.*;
import gencon.robolib.RFTS.RFTSRobot;
import gencon.robolib.RISK.RISKRobot;
import gencon.utils.ScannerListener;
import gencon.utils.Utils;

/**
 * The controller class, that regulates the operation of the system as a whole.
 * This class is run by any harness, by first constructing it (with optional arguments),
 * and then using the <code>run()</code> method (possibly in a separate thread). 
 *
 * @author Victor Ivri
 */
public class Master implements Runnable
{
	/**
	 * The rulesets supported by this client. 
	 */
	public static enum RULESET
	{
		RFTS, RISK;
	}
	
	
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
	
	
	//connection related
	public final Client CLIENT;
	
	//game-related
	private RULESET ruleset;
	private FullGameStatus game_status;
	private Robot robot;
	private int turn;
	private String myUsername;
	private short difficulty;
	private String genomeFileClasspath;
	

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
		pl("Welcome to GenCon (Genetic Conquest): An AI Client for Thousand Parsec : RFTS and Risk rulesets.");
		
		//initializing input listener.
		in.activate(this);
		pl("To quit at any time, enter 'q', then press RETURN.");
		
		//initializing client
		try
		{
			CLIENT.init(args);
		}
		catch (Exception e)
		{
			exit("Failed initializing client.", ABNORMAL_EXIT, e);
		}
		
		//extracting genotype from file:
		Genotype genotype = null;
		try
		{
			genotype = new Genotype(genomeFileClasspath);
		}
		catch (Exception e)
		{
			exit("Failed to extract genotype from file.", ABNORMAL_EXIT, e);
		}
		
		//initializing game status and robot
		if (ruleset == RULESET.RFTS)
		{
			game_status = new FullGameStatusRFTS(this);
			game_status.init();
			robot = new RFTSRobot(genotype, CLIENT, (FullGameStatusRFTS)game_status, difficulty, getTurn());
		}
		else
		{
			game_status = new FullGameStatusRISK(this);
			game_status.init();
			robot = new RISKRobot(genotype, CLIENT, (FullGameStatusRISK)game_status, difficulty);
		}
			
		
		pl("Done initializing GenCon.");
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
		while (true)  //if the 'exit' method is invoked at any point, the program will be killed on its own.
		{
			pr("?");
			delayUntilNewTurn(); //wait till start of new turn.
			pl("!");
			CLIENT.pushTurnStartFlag(); //push the flag back to its place!
			startOfTurnRoutine();
		}
	}
	
	
	/*
	 * DELAYS START OF NEW TURN, UNTIL GENCON HAS ENOUGH TIME TO OPERATE.
	 */
	private void delayUntilNewTurn()
	{
		while (!CLIENT.isTurnStart())
		{
			try
			{
				Thread.sleep(500);
			}
			catch (InterruptedException e)
			{
				exit("Thread interrupted", ABNORMAL_EXIT, e);
			}
		}
	}
	
	private void startOfTurnRoutine()
	{
		try
		{
			int time = CLIENT.getTimeRemaining();
			pl("Start of turn routine commencing... " + time + " seconds to end of turn.");
			game_status.incrementTurn();
			checkIfImAlive(); //check if I'm alive!
			robot.startTurn(time);
			//CLIENT.eventLogger.dumpLogStd();
		}
		catch (Exception e) 
		{
			exit("Unexpected failure to play turn.", ABNORMAL_EXIT, e);
		}
	}
	
	/*
	 * Check if this player is still visible, meaning alive.
	 */
	private void checkIfImAlive()
	{
		if (!game_status.checkIfImAlive())
			exit("WIPED OUT. Sorry boss, there was just too many of them.", NORMAL_EXIT, null);
	}

	/**
	 * Properly exits GenCon.
	 * 
	 * @param message To be displayed on exit.
	 * @param exitType Specifies if it was a normal, or abnormal exit (0 or otherwise).
	 * @param e The exception, which triggered the exit, if it exists.
	 */
	public synchronized void exit(String message, int exitType, Exception e)
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
	
	public RULESET getRuleset()
	{
		return ruleset;
	}
	
	public void setRuleset(RULESET rs)
	{
		ruleset = rs;
	}
	
	/**
	 * @return A copy of the {@link FullGameStatus}.
	 * Note that copy may vary in depth; use with care!
	 */
	public FullGameStatus getStatus()
	{
		return game_status;
	}
	
	/**
	 * @return The absolute turn number, specified by the server.
	 */
	public int getTurn() 
	{
		return turn;
	}

	public void setTurn(int turn) 
	{
		this.turn = (byte)turn;
	}
	
	public short getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(short difficulty) {
		this.difficulty = difficulty;
	}

	public String getGenomeFileClasspath() {
		return genomeFileClasspath;
	}

	public void setGenomeFileClasspath(String genomeFileClasspath) {
		this.genomeFileClasspath = genomeFileClasspath;
	}

	public String getMyUsername() {
		return myUsername;
	}

	public void setMyUsername(String myUsername) {
		this.myUsername = myUsername;
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
