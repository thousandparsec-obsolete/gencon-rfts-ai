package gencon.gamelib;

import java.util.ArrayList;


public class FullGameStatus
{
	private UniverseTree currentGameTree;
	private ArrayList<UniverseTree> gameHistory;
	private Players players;
	public final short DIFFICULTY;
	
	public FullGameStatus(short difficulty, Body universeRoot, String playerName)
	{
		currentGameTree = new UniverseTree(universeRoot);
		players = new Players(playerName);
		DIFFICULTY = difficulty;
	}
}
