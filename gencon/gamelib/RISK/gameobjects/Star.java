package gencon.gamelib.RISK.gameobjects;

import java.util.Collection;
import java.util.HashSet;

import gencon.gamelib.Players.Game_Player;

/**
 * A class that represents a combination of Star System and its single Planet in Risk.
 * It carries the Name and Id of the Star System, and the Army and Owner of the Planet.
 * 
 * It also contains a {@link Collection} of the game-ids of the adjacent {@link Star}s, stored as {@link Integer}s.
 * 
 * @author Victor Ivri
 */
public class Star extends RiskGameObject
{
	private Game_Player owner;
	private int army;
	
	private Collection<Integer> adjacent;
	
	public Star(String name, int gameId)
	{
		super(name, gameId);
	}
	
	public Star(Star other)
	{
		super(new String(other.NAME), other.GAME_ID);
		this.owner = other.getOwner();
		this.army = other.getArmy();
		this.adjacent = other.getAdjacencies();
	}
	
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
	
	public Collection<Integer> getAdjacencies()
	{
		Collection<Integer> returned = new HashSet<Integer>();
		
		for (Integer id : adjacent)
			returned.add(id);
		
		return returned;
	}
	
	public void addAdjacent(Integer star_id)
	{
		adjacent.add(new Integer(star_id));
	}
	
	
	public boolean equals(Star a, Star b)
	{
		return a.GAME_ID == b.GAME_ID && a.NAME.equals(b.NAME) && a.getArmy() == b.getArmy() && a.getOwner().NUM == b.getOwner().NUM;
		//doesn't check for adjacencies, but should be fine.
	}
}
