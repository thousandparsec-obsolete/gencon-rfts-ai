package gencon;

import java.io.PrintStream;
import java.util.Scanner;

import net.thousandparsec.netlib.SequentialConnection;
import net.thousandparsec.netlib.Visitor;

import gencon.clientLib.*;
import gencon.gamelib.FullGameStatus;
import gencon.utils.*;

public class Master <V extends Visitor> implements Runnable
{
	//in/out
	public final static ScannerListener in = new ScannerListener(new Scanner(System.in));
	public final static PrintStream out = System.out;
	public final static String QUIT = "q";
	
	
	//maintanance
	private boolean verboseDebugMode;
	public final static int NORMAL_EXIT = 0;
	public final static int ABNORMAL_EXIT = -1;
	
	
	//connection related
	private Client<V> client;
	
	
	private FullGameStatus gameStatus;
	
	
	public Master(String[] args)
	{
		init(args);
	}
	
	private void init(String[] args) 
	{
		out.println("Welcome to GenCon (Genetic Conquest): An AI Client for Thousand Parsec : RFTS ruleset.");
		
		//initializing input listener.
		in.activate(this);
		out.println("To quit at any time, enter 'q', then press RETURN.");
		
		//initializing client
		client = new Client<V>(this);
		try
		{
			client.runClient(args);
		}
		catch (Exception e)
		{
			exit("Failed initializing client.", ABNORMAL_EXIT, e);
		}
		
		
		//initializing game status
		gameStatus = new FullGameStatus(client.getDifficulty(), client.getPlayerName());
		
		
		
		out.println("Done initializing GenCon.");
	}
	
	
	public void run()
	{
		
		
		gameCycle();
	}
	
	private void gameCycle()
	{
		
	}
	
	private void startOfTurnRoutine()
	{
		
	}
	
	private void startRobot(SequentialConnection<V> conn)
	{
		
	}
	
	private void stopRobot()
	{
		
	}
	
	
	public void exit(String message, int exitType, Exception e)
	{
		out.println("\n______________________________________");
		out.println("Exiting GenCon. Reason: " + message);
		
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
			client.exit();
			
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
	
	public void setVerboseDebugMode(boolean mode)
	{
		verboseDebugMode = mode;
	}
	
	public boolean isVerboseDebugMode()
	{
		return verboseDebugMode;
	}
}
