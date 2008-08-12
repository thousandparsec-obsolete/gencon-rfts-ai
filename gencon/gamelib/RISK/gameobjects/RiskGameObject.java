package gencon.gamelib.RISK.gameobjects;

public abstract class RiskGameObject 
{
	public final String NAME;
	public final int GAME_ID;
	
	public RiskGameObject(String name, int gameId)
	{
		NAME = name;
		GAME_ID = gameId;
	}
}
