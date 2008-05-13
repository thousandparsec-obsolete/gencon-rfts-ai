package gencon;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.Future;

import net.thousandparsec.netlib.*;
import net.thousandparsec.netlib.tp03.*;

public class Client extends TP03Visitor 
{
	//maintanence
	private String URIString;
	private Connection<TP03Visitor> conn;
	private PrintStream stout = System.out; 
	private Scanner stin = new Scanner(System.in);
	
	//game-related
	private int difficulty;
	
	Client() {}
	
	/**
	 * 
	 * @param conStr the string that specifies the address of the server, the username, and the password
	 */
	void init(String UStr)
	{
		this.URIString = UStr;
	}
	
	/**
	 * 
	 * Runs the client.
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 * @throws TPException
	 * 
	 */
	void run()
	{
		//first establish a connection with the server
		establishConnection();

		//execute command-line orders, until encountering control character 'q'
		stout.println("Enter command. 'list' shows all available commands.");	
		
		while(true == true)
		{
			stout.print("Enter command > ");
			String command = stin.nextLine();
			
			if (command.equals("q")){
				exit("Manual exit from client.");
			} 
			else if (command.equals("list")){
				printCommands();
			}
			else if (command.equals("start")){
				setDifficulty(5);
				startPlay();
			}
			else if (command.length() == 6 && 
					command.substring(0, 4).equals("diff") && 
					command.substring(5, 6).matches("[1-9]")){
				try
				{
					setDifficulty(command.charAt(5));
				}
				catch (Exception e)
				{
					printInvalid();
				}
			}
			
			else {
				printInvalid();
			}
		}
	}
	
	/**
	 * Prints out list of accepted commands.
	 */
	private void printCommands()
	{
		stout.println("> The list of available commands <");
		stout.println("'q' - close connection, and exit client.");
		stout.println("'list' - lists all available commands.");
		stout.println("'start' - start playing the game. Default difficulty 5.");
		stout.println("'diff arg' - set difficulty; arg = 1 --> 9");
		
		stout.println("> ------------------------------ <");
		
	}
	
	/**
	 * Prints out message of invalid command.
	 */
	private void printInvalid()
	{
		stout.println("Invalid command. For full list of accepted commands, enter 'list'");
	}
	
	
	
/**
 * Establishes a connection with the server.
 * 
 * @throws UnknownHostException
 * @throws IOException
 * @throws URISyntaxException
 * @throws InterruptedException
 * @throws TPException
 */
	private void establishConnection()
	{
		try
		{
			stout.print("Establishing connection to server... ");
		
			TP03Decoder decoder = new TP03Decoder();
			conn = decoder.makeConnection(new URI(URIString), true, new TP03Visitor(true));
			DefaultConnectionListener<TP03Visitor> listener = new DefaultConnectionListener<TP03Visitor>();
			conn.addConnectionListener(listener);
		
			stout.println("connection established.");
		}
		catch (Exception e)
		{
			stout.println("Error connecting to server.");
			stout.println(e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	/**
	 * Sets the difficulty of the AI opponent.
	 * @param diff between 1 --> 9.
	 */
	private void setDifficulty(int diff)
	{
		this.difficulty = diff;
	}
	
	/**
	 * Start playing game.
	 */
	private void startPlay()
	{
		stout.println("Starting to play game... ");
		//recieveFramesSynch();
	}
	
	/*
	private void recieveFramesSynch()
	{
		try
		{
			stout.print("Recieving all frames synchronously... ");
			conn.receiveAllFrames(this);
			stout.println("done.");
		}
		catch (Exception e)
		{
			stout.println("Failed to synchronously fetch frames");
			stout.println(e.getMessage());
			e.printStackTrace();
		}
	}
	*/
	
	
	
	
	/**
	 * Closing connection, and exiting client.
	 * @param message Exit message.
	 */
	private void exit(String message)
	{
		//closing the connection, and shutting down the client.
		try
		{
			stout.println("Exit: " + message);
			stout.print("Closing connection... ");
			conn.close();
			stout.println("done.");
			stout.println("Farewell, farewell; goodbye is such sweet sorrow.");
			System.exit(0);
		}
		catch (IOException e)
		{
			stout.println("Error closing the connection.");
			stout.println(e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
}
