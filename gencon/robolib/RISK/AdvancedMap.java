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
	private Players players;
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
		players = status.right;
		
		generateAdvancedStars();
		generateThreatsToAdvStars();
	}
	
	public void generateAdvancedStars()
	{
		Collection<Star> stars = basicMap.getStars();
		advancedStars = new HashSet<AdvancedStar>(stars.size());
		
		for (Star star : stars)
			advancedStars.add(new AdvancedStar(star));
	}
	
	public void generateThreatsToAdvStars()
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
			
			//iterating over neighbors:
			for (Star neighbor : adjacent)
			{
				enemyForces += neighbor.getArmy();
				
				if (neighbor.getOwner() != -1 && neighbor.getOwner() != as.STAR.getOwner() 
						&& !enemies.contains(neighbor.getOwner()))
					enemies.add(neighbor.getOwner());
			}
			
			//making the calculations:
			double threat = enemyForces / forces;
			byte enemyCount = (byte)enemies.size();
			
			//setting values:
			as.setThreat(threat);
			as.setThreatDiversity(enemyCount);
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
		private double threat;
		private byte threat_diversity; //how many players threaten it?
		
		public AdvancedStar(Star star)
		{
			STAR = star;
		}
		
		public AdvancedStar(AdvancedStar other)
		{
			this.STAR = new Star(other.STAR);
			this.threat = other.getThreat();
			this.threat_diversity = other.getThreatDiversity();
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
	}
	
	
}
