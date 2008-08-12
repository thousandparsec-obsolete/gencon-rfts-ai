package gencon.gamelib.RISK.gameobjects;

import java.util.Collection;
import java.util.HashSet;

public class Constellation extends RiskGameObject
{
	private Collection<Integer> stars;
	
	public Constellation(String name, int gameId, Collection<Integer> containsStars)
	{
		super(name, gameId);
		stars = containsStars;
	}
	
	public Constellation(Constellation other)
	{
		super(other.NAME, other.GAME_ID);
		this.stars = other.getStars();
	}
	
	public Collection<Integer> getStars()
	{
		Collection<Integer> returned = new HashSet<Integer>();
		
		for (Integer i : stars)
			returned.add(new Integer(i));
		
		return returned;
	}
}
