package gencon.gamelib.RISK.gameobjects;

import java.util.Collection;
import java.util.HashSet;

/**
 * A class that represents a combination of Star System and its single Planet in Risk.
 * It carries the Name of the Star System, its Id, as well as the Id, Army and Owner of the Planet.
 * The Id of the Star System is {@link Star}.GAME_ID, and the Id of its Planet is {@link Star}.PlANET_ID.
 * 
 * It also contains a {@link Collection} of the game-ids of the adjacent {@link Star}s, stored as {@link Integer}s.
 * 
 * @author Victor Ivri
 */
public class Star extends RiskGameObject
{
	private int owner;
	private int army;
	private int reinforcements;
	
	private Collection<Integer> adjacent;
	
	public Star(String name, int starId)
	{
		super(name, starId);
		adjacent = new HashSet<Integer>();
	}
	
	public Star(Star other)
	{
		super(new String(other.NAME), other.GAME_ID);
		this.owner = other.getOwner();
		this.army = other.getArmy();
		this.reinforcements = other.getReinforcementsAvailable();
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
	
	public int getReinforcementsAvailable()
	{
		return reinforcements;
	}
	
	public void setAvailableReinforcements(int amount)
	{
		reinforcements = amount;
	}
	
	
	public int getOwner()
	{
		return owner;
	}
	
	public void setOwner(int newOwner)
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
	
	public void addAdjacent(int star_id)
	{
		adjacent.add(new Integer(star_id));
	}
	
	
	public boolean equals(Star a, Star b)
	{
		return a.GAME_ID == b.GAME_ID && a.NAME.equals(b.NAME) && a.getArmy() == b.getArmy() && a.getOwner() == b.getOwner();
		//doesn't check for adjacencies, but should be fine.
	}
}
