package gencon.robolib.RISK;

import java.util.Collection;
import java.util.HashSet;

import net.thousandparsec.util.Pair;
import gencon.gamelib.Players;
import gencon.gamelib.RISK.FullGameStatusRISK;
import gencon.gamelib.RISK.UniverseMap;
import gencon.gamelib.RISK.gameobjects.Star;

public class AdvancedMap 
{
	private UniverseMap basicMap;
	private final FullGameStatusRISK FGS;
	
	private Collection<AdvancedStar> advancedStars;
	
	public AdvancedMap(FullGameStatusRISK status)
	{
		FGS = status;
	}
	
	public void updateMap()
	{
		Pair<UniverseMap, Players> status = FGS.getCurrentStatus();
		basicMap = status.left;
		
		generateAdvancedStars();
		generateParametersOfAdvStars();
	}
	
	public void generateAdvancedStars()
	{
		Collection<Star> stars = basicMap.getStars();
		advancedStars = new HashSet<AdvancedStar>(stars.size());
		
		for (Star star : stars)
			advancedStars.add(new AdvancedStar(star));
	}
	
	public void generateParametersOfAdvStars()
	{
		for (AdvancedStar as : advancedStars)
		{
			//collecting neighbors:
			Collection<Star> adjacent = new HashSet<Star>();
			for (Integer adjId : as.STAR.getAdjacencies())
				adjacent.add(basicMap.getStarWithId(adjId));
			
			//getting friendly forces on planet:
			int forces = as.STAR.getArmy();
			if (forces == 0) //need to eliminate the 0.
				forces++;
			
			int enemyForces = 0;
			Collection<Integer> enemies = new HashSet<Integer>();
			boolean backwaters = true;
			
			//iterating over neighbors:
			for (Star neighbor : adjacent)
			{
				if (neighbor.getOwner() != as.STAR.getOwner() && !enemies.contains(neighbor.getOwner()))
				{
					enemyForces += neighbor.getArmy();
					enemies.add(neighbor.getOwner());
				}
				
				else if (neighbor.getOwner() == as.STAR.getOwner()) //if it's friendly to it.
					enemyForces -= neighbor.getArmy();
				
				backwaters = backwaters && neighbor.getOwner() == as.STAR.getOwner();
			}
			
			//making the calculations:
			double threat = enemyForces / forces;
			byte enemyCount = (byte)enemies.size();
			
			//setting values:
			as.setThreat(threat);
			as.setThreatDiversity(enemyCount);
			as.setBackwaters(backwaters);
		}
	}
	
	
	public AdvancedStar getAdvancedStarWithId(int id)
	{
		AdvancedStar star = null;
		for (AdvancedStar as : advancedStars)
			if (as.STAR.GAME_ID == id)
				star = new AdvancedStar(as);
		
		return star;
	}
	
	
	
	public static class AdvancedStar
	{
		public final Star STAR;
		private double threat; //how much is it in danger?
		private byte threat_diversity; //how many players threaten it?
		private boolean backwaters;
		
		public AdvancedStar(Star star)
		{
			STAR = star;
		}
		
		public AdvancedStar(AdvancedStar other)
		{
			this.STAR = new Star(other.STAR);
			this.threat = other.getThreat();
			this.threat_diversity = other.getThreatDiversity();
			this.backwaters = other.getBackwaters();
		}
		
		public double getThreat()
		{
			return threat;
		}
		
		public void setThreat(double value)
		{
			threat = value;
		}
		
		public byte getThreatDiversity()
		{
			return threat_diversity;
		}
		
		public void setThreatDiversity(byte value)
		{
			threat_diversity = value;
		}
		
		public boolean getBackwaters()
		{
			return backwaters;
		}
		
		public void setBackwaters(boolean value)
		{
			backwaters = value;
		}
	}
	
	
}
