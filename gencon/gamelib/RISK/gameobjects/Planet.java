package gencon.gamelib.RISK.gameobjects;

import gencon.gamelib.Players.Game_Player;

public class Planet 
{
	private Game_Player owner;
	private int army;
	
	public Planet(){}
	
	public int getArmy()
	{
		return army;
	}
	
	public void setArmy(int newArmy)
	{
		army = newArmy;
	}
	
	public Game_Player getOwner()
	{
		return owner;
	}
	
	public void setOwner(Game_Player newOwner)
	{
		owner = newOwner;
	}
}
