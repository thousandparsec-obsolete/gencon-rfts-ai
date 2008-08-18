package gencon.gamelib;

import gencon.clientLib.Client;

import java.io.IOException;

import net.thousandparsec.netlib.TPException;

public interface FullGameStatus 
{	
	/**
	 * Initializes at the start of the game.
	 * 
	 * @throws IOException
	 * @throws TPException
	 */
	public void init() throws IOException, TPException;
	
	/**
	 * Updates game-world, using a {@link Client}. 
	 * To be summoned at the start of turn.
	 * 
	 * @throws IOException
	 * @throws TPException
	 */
	public void incrementTurn() throws IOException, TPException;
	
	/**
	 * @return True if this player is in the game.
	 */
	public boolean checkIfImAlive();
	
	/**
	 * @return A copy of this {@link FullGameStatus}.
	 */
	public FullGameStatus copyStatus();
	
}
