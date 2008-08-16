package gencon.robolib.RISK;

import gencon.gamelib.RISK.FullGameStatusRISK;
import gencon.gamelib.RISK.gameobjects.Star;
import gencon.robolib.RISK.AdvancedMap.AdvancedStar;

import java.util.Collection;
import java.util.List;

public class HigherLevelActions 
{
	private final ActionMethods ACTIONS;
	private final AdvancedMap MAP;
	private final FullGameStatusRISK FGS;
	
	private final double DOUBLE_TOLERANCE = 1e-3;
	
	public HigherLevelActions(FullGameStatusRISK fgs, ActionMethods actions, AdvancedMap map)
	{
		FGS = fgs;
		ACTIONS = actions;
		MAP = map;
	}
	
	/**
	 * 
	 * @param num_of_planets Number of planets to be reinforced.
	 * @param importance Between 0 and 1, inclusive; 0 would give 0 reinforcements, while 1 would give all available reinforcements.
	 * @return true if all went well, false if orders failed.
	 */
	public boolean ReinforceEndangeredPlanets(int num_of_stars, double importance)
	{
		assert (importance - DOUBLE_TOLERANCE) > 0.0 && (importance + DOUBLE_TOLERANCE) < 1.0;

		//get list of stars by threats:
		Collection<AdvancedStar> myStars = MAP.getStarsOfPlayer(MAP.getAllAdvStars(), FGS.getCurrentStatus().right.getMe().NUM);
		List<AdvancedStar> riskList = MAP.sortByThreat(myStars);
		
		//register threat for each of n-stars:
		double[] threat_by_star = new double[num_of_stars];
		for (int i = 0; i < num_of_stars; i++)
			threat_by_star[i] = riskList.get(i).getThreat();
		
		//determine the total-threat:
		double totalThreat = 0;
		for (int i = 0; i < threat_by_star.length; i++)
			totalThreat += threat_by_star[i];
		
		
		//determine how much of the total should be spent overall:
		int totalReinforcements = (int) Math.round(Math.floor(FGS.getReinforcements() * importance));
		
		if (totalReinforcements == 0)
			return true; //nothing to be done, but nothing went wrong!
		
		/*
		 * allocate reinforcement resources to each star based on the following formula:.
		 * 
		 * (totalRainforcements / totalThreat) * threatOnStarN
		 * 
		 * s.t.: (totalRainforcements / totalThreat) * (threatOnStarN + threatOnStarN+1 + threatOnStarN+2 ...) = totalReinforcements.
		 */
		
		double reinforceCoefficient = totalReinforcements / totalThreat;
		
		//to gauge if anything went wrong!
		boolean success = true;
		
		//allocate the reinforcements, and send the orders!
		for (int i = 0; i < num_of_stars; i++)
		{
			int reinforce = (int) Math.round(Math.floor(reinforceCoefficient * threat_by_star[i]));
			
			if (reinforce > 0)
			{
				Star star = riskList.remove(0).STAR; //always remove the head!
				
				try
				{
					success = success && ACTIONS.orderReinforce(star, reinforce, false);
				}
				catch (Exception e)
				{
					success = false;
				}
			}
		}
		
		return success;
	}
	
}
