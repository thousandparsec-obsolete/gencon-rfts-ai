package gencon.clientLib;

import gencon.gamelib.Players.Game_Player;
import net.thousandparsec.netlib.tp03.Player;

public abstract class ObjectConverterGeneric 
{
	
	public synchronized static Game_Player convertPlayer(Player player)
	{
		return new Game_Player(player.getId(), player.getName());
	}
}
