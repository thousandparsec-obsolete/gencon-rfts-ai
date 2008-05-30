package gencon.utils;

import gencon.Client;

import java.util.*;

/**
 * A wrapper class for {@link Scanner}. 
 * Periodically (every second) listens on the input scanner in a separate thread, 
 * to see whether the special QUIT string has been encountered in the {@link Client}.
 * 
 * @author Victor Ivri
 */
public class ScannerListener
{
	private final Scanner scanner;
	private final Client client;
	Listener listen;
	Thread listenThread;
	
	/*
	 * Dummy constructor
	 */
	private ScannerListener(){scanner = null; client = null;}
	
	/**
	 * The sole constructor.
	 * 
	 * @param sc usually, a new {@link Scanner}.
	 * @param cl the specific {@link Client} that's meant to be monitored.
	 */
	public ScannerListener(Scanner sc, Client cl)
	{
		scanner = sc;
		client = cl;
		
		listen = new Listener();
		
		listenThread = new Thread(listen);
		listenThread.start();
	}
	
	/**
	 * Same contract as in {@link Scanner}.next()
	 */
	public String next() throws NoSuchElementException, IllegalStateException
	{
		monitor();
		return scanner.next();
	}
	
	/**
	 * Same contract as in {@link Scanner}.nextInt()
	 */
	public int nextInt() throws InputMismatchException, NoSuchElementException, IllegalStateException
	{
		monitor();
		return scanner.nextInt();
	}
	
	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * 
	 * ADDITIONAL METHODS, SUCH AS NextLong() ETC CAN BE IMPLEMENTED IN THE SAME MANNER.
	 * 
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 */
	
	/**
	 * Interrupts the listening thread.
	 */
	public void close()
	{
		listenThread.interrupt();
	}

	/*
	 * Monitors for the exit string.
	 */
	private void monitor()
	{
		if (scanner.hasNext(Client.QUIT))
			listen.quit();
	}
	
	
	/**
	 * Inner class that runs on a separate thread, monitoring the ScannerListener every second (1000ms).
	 * @author Victor Ivri
	 *
	 */
	class Listener implements Runnable
	{
		private boolean quit;
		
		Listener()
		{
			quit = false;
		}
		
		public void run() 
		{
			while (true)
			{
				try
				{
					Thread.sleep(10);
					monitor();
					// encountered exit string; exiting client.
					if (quit)
					{
						
						client.exitOnEncounteringExitString();
						return;
					}
				}
				catch (InterruptedException e)
				{
					System.err.println("Thread interrupted.");
					return; //return if interrupted
				}
			}
		}
		
		public void quit()
		{
			quit = true;
		}
	}
}