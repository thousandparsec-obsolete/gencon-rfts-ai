package gencon.robolib.RISK;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import gencon.gamelib.Players.Game_Player;
import gencon.gamelib.RISK.UniverseMap;
import gencon.gamelib.RISK.gameobjects.Star;
import gencon.utils.DebugOut;

public class AdvancedMap 
{
	private UniverseMap uMap;
	private Collection<AdvancedStar> advancedStars;
	
	public AdvancedMap(){}
	
	public void updateMap(UniverseMap map, int myPlrNum)
	{
		uMap = map;
		generateAdvancedStars();
		generateParametersOfAdvStars(myPlrNum);
	}
	
	private void generateAdvancedStars()
	{
		Collection<Star> stars = uMap.getStars();
		advancedStars = new HashSet<AdvancedStar>(stars.size());
		
		for (Star star : stars)
			advancedStars.add(new AdvancedStar(star));
	}
	
	private void generateParametersOfAdvStars(int myPlrNum)
	{
		for (AdvancedStar as : advancedStars)
		{
			//collecting neighbors:
			Collection<Star> adjacent = new HashSet<Star>();
			for (Integer adjId : as.STAR.getAdjacencies())
				adjacent.add(uMap.getStarWithIdUnsafe(adjId));
			
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
				if (neighbor.getOwner() != as.STAR.getOwner() && neighbor.getOwner() != -1
						&& neighbor.getOwner() != myPlrNum) //if it's not friendly to it, not neutral, and not mine.
				{
					enemyForces += neighbor.getArmy();
					
					if (!enemies.contains(neighbor.getOwner())) //add to enemy list, if it's not already there!
						enemies.add(neighbor.getOwner());
				}
				
				backwaters = backwaters && neighbor.getOwner() == as.STAR.getOwner(); //only true if all are friendly to it.
			}
			
			//counting number of enemies:
			int enemyCount = enemies.size();
			if (enemyCount < 1) //minimum 1, for calculation.
				enemyCount = 1;
			
			/*
			 * Calculating threat based on the amount of enemy soldiers nearby, 
			 * and the amount of different players the threat comes from.
			 * (More players means lower threat)
			 * Also, checking if the planet is solely surrounded by friendly planets.
			 * If so, it's marked 'backwaters'.
			 */
			//the threat formula:
			double threat = (enemyForces - forces) * (1 /enemyCount);
			
			//setting values:
			as.setThreat(threat);
			as.setBackwaters(backwaters);
		}
	}
	
	public void printData(DebugOut out, Collection<Game_Player> players)
	{
		out.pl("\n- - -");
		out.pl("Universe data:");
		
		for (Game_Player pl : players)
		{
			int i = pl.NUM;
			String name = pl.NAME;
			String owner = "Stars owned by player " + name + " (" + i + "): ";
			if (i == -1)
				owner = "Neutral stars: ";
			out.pr(owner);
			Collection<AdvancedStar> stars = getStarsOfPlayer(advancedStars, i);
			
			int totalStrength = 0;
			for (AdvancedStar as : stars)
			{
				out.pr("<" + as.STAR.GAME_ID + "> ");
				totalStrength += as.STAR.getArmy();
			}
			out.pl("");
			if (i != -1)
			{
				int reinforcements = 0;
				if (!uMap.getStarsOfPlayer(i).isEmpty())
					reinforcements = uMap.getStarsOfPlayer(i).iterator().next().getReinforcementsAvailable();
				out.pl("	Total stars: " + stars.size() + "; Total army strength: " + totalStrength + "; Total reinforcements: " + reinforcements);
			}
		}
		out.pl("- - -");
	}
	
	public void printDataFull(DebugOut out)
	{
		out.pl("\n>>>");
		out.pl("Generating full output for Advanced Map:");
		for (AdvancedStar star : advancedStars)
			out.pl(star.toString());
		out.pl(">>>");
	}
	
	
	
	public UniverseMap getBasicMap()
	{
		return uMap;
	}
	
	public AdvancedStar getAdvancedStarWithId(int id)
	{
		AdvancedStar star = null;
		for (AdvancedStar as : advancedStars)
			if (as.STAR.GAME_ID == id)
			{
				star = as;
				break;
			}
		
		//System.out.print("Star with id " + id + ": ");
		//System.out.println(star.toString());
		
		return star;
	}
	
	/**
	 * @return A {@link List} of {@link AdvancedStar}s, sorted high-to-low with respect to {@link AdvancedStar}.getThreat().
	 * 
	 * Sort implemented: bubble-sort.
	 */
	public List<AdvancedStar> sortByThreat(Collection<AdvancedStar> unsorted)
	{
		List<AdvancedStar> sorted = new ArrayList<AdvancedStar>();
		
		while (!unsorted.isEmpty())
		{
			Iterator<AdvancedStar> iterator = unsorted.iterator();
			AdvancedStar highest = iterator.next();
			
			while (iterator.hasNext())
			{
				AdvancedStar possibleHighest = iterator.next();
				if (possibleHighest.getThreat() > highest.getThreat())
					highest = possibleHighest;
			}
			sorted.add(highest);
			unsorted.remove(highest);
		}
		
		return sorted;
	}
	
	public Collection<AdvancedStar> getAllBackwaters(int plrId)
	{
		Collection<AdvancedStar> backwaters = new HashSet<AdvancedStar>();
		
		for (AdvancedStar as : advancedStars)
			if (as.STAR.getOwner() == plrId && as.getBackwaters())
				backwaters.add(as);
		
		return backwaters;
	}
	
	public Collection<AdvancedStar> getAllAdvStars()
	{
		Collection<AdvancedStar> returned = new HashSet<AdvancedStar>();
		
		for (AdvancedStar as : advancedStars)
			returned.add(as);
		
		return returned;
	}
	
	public Collection<AdvancedStar> getStarsOfPlayer(Collection<AdvancedStar> someStars, int playerId)
	{
		Collection<AdvancedStar> returned = new HashSet<AdvancedStar>();
		
		for (AdvancedStar as : someStars)
			if (as.STAR.getOwner() == playerId)
				returned.add(as);
		
		return returned;
	}
	
	public Collection<AdvancedStar> getNeighbors(AdvancedStar advStar)
	{
		Collection<AdvancedStar> neighbors = new HashSet<AdvancedStar>();
		
		Collection<Integer> neighborStars = advStar.STAR.getAdjacencies();
		
		for (Integer i : neighborStars)
			neighbors.add(getAdvancedStarWithId(i));
		
		return neighbors;
	}
	
	
	public static class AdvancedStar
	{
		public final Star STAR;
		private double threat; //how much is it in danger?
		private boolean backwaters;
		
		public AdvancedStar(Star star)
		{
			STAR = star;
		}
		
		public AdvancedStar(AdvancedStar other)
		{
			this.STAR = new Star(other.STAR);
			this.threat = other.getThreat();
			this.backwaters = other.getBackwaters();
		}
		
		/**
		 * 
		 * @return The net number of unfriendly troops that surround the star. Does not include my forces.
		 */
		public double getThreat()
		{
			return threat;
		}
		
		public void setThreat(double value)
		{
			threat = value;
		}
		
		public boolean getBackwaters()
		{
			return backwaters;
		}
		
		public void setBackwaters(boolean value)
		{
			backwaters = value;
		}
		
		@Override
		public String toString()
		{
			return "Advanced star data: Star toString: [" + STAR.toString() + "]. Threat: " + getThreat() + "; Backwaters: " + getBackwaters();
		}
	}
	
	
}
