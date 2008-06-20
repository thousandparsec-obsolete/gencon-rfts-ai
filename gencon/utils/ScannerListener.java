package gencon.utils;

import gencon.Master;

import java.io.IOException;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * A wrapper class for {@link Scanner}. 
 * Periodically (every second) listens on the input {@link Scanner} in a separate thread, 
 * to see whether the special QUIT string has been encountered in {@link System}.in.
 * 
 * @author Victor Ivri
 */
public class ScannerListener
{
	private final Scanner scanner;
	private Master master;
	private final Thread listenThread;
	
	private boolean quit = false;
	private boolean scannerLocked = false;
	
	/**
	 * The constructor.
	 * 
	 * @param sc usually, a new {@link Scanner}.
	 * @param cl the specific {@link Client} that's meant to be monitored.
	 */
	public ScannerListener(Scanner sc)
	{
		scanner = sc;
		
		listenThread = new Thread(new Listener());
		listenThread.start();
	}
	
	/**
	 * Must call this method for this {@link ScannerListener} to be operational.
	 * @param master The target to be shut down in case of exit string.
	 */
	public void activate (Master master)
	{
		this.master = master;
	}
	
	/**
	 * Same contract as in {@link Scanner}.next(),
	 * except when the exit string is encountered.
	 * In that case, the listener is notified to close the client.
	 * 
	 * This method is thread-safe.
	 */
	public String next() throws NoSuchElementException, IllegalStateException
	{
		String in;
		scannerLocked = true;
		synchronized (scanner)
		{
			in = scanner.next();
			if (in.equals(Master.QUIT))
				quit = true;
		}
		scannerLocked = false;
		return in;
	}
	
	/**
	 * Same contract as in {@link Scanner}.nextInt(),
	 * except when the exit string is encountered.
	 * In that case, the listener is notified to close the client.
	 * 
	 * This method is thread-safe.
	 */
	public int nextInt() throws InputMismatchException, NoSuchElementException, IllegalStateException
	{
		String in;
		scannerLocked = true;
		synchronized (scanner)
		{
			in = scanner.next();
			if (in.equals(Master.QUIT))
				quit = true;
		}
		scannerLocked = false;
		
		return new Integer(in).intValue();
	}
	
	/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * 
	 * ADDITIONAL METHODS, SUCH AS NextLong() ETC CAN BE IMPLEMENTED IN THE SAME MANNER.
	 * 
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 */
	
	/**
	 * Interrupts the listening thread.
	 * Meant to be activated by the {@link Client}, to terminate the other thread in an orderly manner.
	 */
	public void close()
	{
		listenThread.interrupt();
		try
		{
			System.in.close();
		}
		catch (IOException ioe)
		{
			Master.out.println(ioe.getMessage());
		}
	}

	/*
	 * Monitors for the exit string. true means it has encountered it.
	 */
	private boolean check()
	{
		if (scannerLocked)  //case: scanner waiting for standard in.
			return quit;
		else				//case: scanner not waiting for standard in.
			return quit || hasNextQuit(); 
	}
	
	/*
	 * Monitors whether the next string is the exit string.
	 */
	private boolean hasNextQuit()
	{
		synchronized (scanner)
		{
			return scanner.hasNext(Master.QUIT);
		}
	}
	
	/**
	 * Inner class that runs on a separate thread, monitoring the ScannerListener every second (1000ms).
	 * @author Victor Ivri
	 *
	 */
	private class Listener implements Runnable
	{
		Listener(){}
		
		public void run() 
		{
			while (true)
			{
				try
				{
					Thread.sleep(1000);
					if (check() && master != null)
					{
						master.exit("Exit string '" + Master.QUIT + "' encountered.", Master.NORMAL_EXIT, null);
						return;
					}
				}
				catch (InterruptedException e)
				{
					return; //return if interrupted
				}
			}
		}
	
	}
}