package gencon.rfts;

public class Player 
{
	public final int PLAYER_NUM;
	public final String PLAYER_NAME;
	
	/**
	 * Standard constructor.
	 * 
	 * @param player_num The player number in the game.
	 * @param player_name The player name in the game.
	 */
	public Player(int player_num, String player_name) 
	{
		PLAYER_NUM = player_num;
		PLAYER_NAME = player_name;
	}
}
