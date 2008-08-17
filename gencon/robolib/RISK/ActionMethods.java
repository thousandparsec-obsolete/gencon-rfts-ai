package gencon.robolib.RISK;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.thousandparsec.netlib.TPException;
import gencon.clientLib.RISK.ClientMethodsRISK;
import gencon.gamelib.RISK.gameobjects.Star;
import gencon.robolib.RISK.AdvancedMap.AdvancedStar;

public class ActionMethods 
{
	private final AdvancedMap MAP;
	private final ClientMethodsRISK CLIENT_RISK;
	
	public ActionMethods(AdvancedMap advMap, ClientMethodsRISK clientRisk)
	{
		MAP = advMap;
		CLIENT_RISK = clientRisk;
	}
	
	public boolean orderMove(Star from, Star to, int troops, boolean urgent) throws TPException, IOException
	{
		return CLIENT_RISK.orderMove(from, to, troops, urgent);
	}
	
	public boolean orderColonize(Star star, int troops, boolean urgent) throws TPException, IOException
	{
		return CLIENT_RISK.orderColonize(star, troops, urgent);
	}
	
	public boolean orderReinforce(Star star, int troops, boolean urgent) throws TPException, IOException
	{
		return CLIENT_RISK.orderReinforce(star, troops, urgent);
	}
	
	
	private final double DOUBLE_TOLERANCE = 1e-3;
	/**
	 * Reinforces N-most-endangered planets, relative to the threat they face.
	 * 
	 * @param num_of_planets Number of planets to be reinforced.
	 * @param gene Can be 0, 1 or 2. Determines the amount of reinforcements to be distributed: 
	 * 	0 (33% of total available reinforcements), 1 (50%), or 2 (66%).
	 * @return true if all went well, false if (at least some) orders failed.
	 */
	public boolean ReinforceEndangeredPlanets(int num_of_stars, byte gene, int myPlrNum)
	{
		assert gene == 0 || gene == 1 || gene == 2;
		
		double importance = 0.0;
		if (gene == 0)
			importance = 0.33333;
		else if (gene == 1)
			importance = 0.5;
		else if (gene == 2)
			importance = 0.66666;
		
		

		//get list of stars by threats:
		Collection<AdvancedStar> myStars = MAP.getStarsOfPlayer(MAP.getAllAdvStars(), myPlrNum);
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
		int totalReinforcements = (int) Math.round(Math.floor(MAP.getBasicMap().getMyReinforcements() * importance));
		
		if (totalReinforcements == 0)
			return true; //nothing to be done, but nothing went wrong!
		
		/*
		 * allocate reinforcement resources to each star based on the following formula:
		 * 
		 * (totalRainforcements / totalThreat) * threatOnStarN
		 * 
		 * s.t.: (totalRainforcements / totalThreat) * (threatOnStarN + threatOnStarN+1 + threatOnStarN+2 ...) = totalReinforcements.
		 */
		
		double reinforceCoefficient = totalReinforcements / totalThreat;
		
		//to gauge if anything went wrong!
		boolean success = true;
		
		//to record the actual reinforcements (may differ due to the 'floor' function)
		int actualTotalReinforced = 0;
		
		//allocate the reinforcements, and send the orders!
		for (int i = 0; i < num_of_stars; i++)
		{
			int reinforce = (int) Math.round(Math.floor(reinforceCoefficient * threat_by_star[i]));
			actualTotalReinforced += reinforce;
			
			if (reinforce > 0)
			{
				Star star = riskList.remove(0).STAR; //always remove the head!
				
				try
				{
					success = success && orderReinforce(star, reinforce, false);
				}
				catch (Exception e)
				{
					success = false;
				}
			}
		}
		
		//adjusting available reinforcements in the map:
		MAP.getBasicMap().setMyReinforcements(MAP.getBasicMap().getMyReinforcements() - actualTotalReinforced);
		
		return success;
	}
	
	
	/**
	 * Transfers troops from backwater planets (ones which are surrounded by friendlies),
	 * to nearby friendly planets, which are endangered. 
	 * 
	 * @param gene Can be 0, 1 or 2. Determines the behavior of troop transfer: 
	 * 	0 means all troops to most endangered planet, 1 means 50% to most endangered, the rest evenly distributed between neighbors,
	 * and 2 evenly distributes between all neighbors.
	 * @return true if all went well, false if (at least some) orders failed.
	 */
	public boolean transferTroopsFromBackwaterStars(byte gene, int myPlrNum)
	{
		assert gene == 0 || gene == 1 || gene == 2;

		boolean success = true; //the value to be returned.
		
		//get all my backwaters.
		Collection<AdvancedStar> backwaters = MAP.getAllBackwaters(myPlrNum);
		
		for (AdvancedStar star : backwaters)
		{
			if (star.STAR.getArmy() - 1 == 0) //no army to give! 
				return true;
				
			//collect all friendly neighbors, which aren't backwater themselves.
			Collection<AdvancedStar> neighbors = MAP.getNeighbors(star);
			for (AdvancedStar as : neighbors)
				if (as.STAR.getOwner() != myPlrNum || as.getBackwaters() == true)
					neighbors.remove(as);
			
			if (neighbors.isEmpty()) //nowhere to send!
				return true;
			
			if (gene == 0 || gene == 1) //gives all troops to most endangered.
			{
				//get the most endangered one:
				Iterator<AdvancedStar> iterator = neighbors.iterator();
				AdvancedStar endangered = iterator.next();
				while (iterator.hasNext())
				{
					AdvancedStar as = iterator.next();
					if (as.getThreat() > endangered.getThreat())
						endangered = as;
				}
				neighbors.remove(endangered); //no need to keep it there!
				
				if (gene == 0) //give all to 
				{
					try
					{
						return orderMove(star.STAR, endangered.STAR, star.STAR.getArmy() - 1, false);
					}
					catch (Exception e)
					{
						return false;
					}
				}
				
				//else: gene == 1. Give 50% to most threatened, then distribute evenly.
				int halfArmy = (int) Math.round(Math.floor((star.STAR.getArmy() - 1) / 2));
				try
				{
					success = success && orderMove(star.STAR, endangered.STAR, halfArmy, false);
				}
				catch (Exception e)
				{
					success = false;
				}
				
				//distributing evenly between rest:
				int remainingArmy = star.STAR.getArmy() - halfArmy;
				int remainingNeighbors = neighbors.size();
				int eachGets = (int) Math.round(Math.floor(remainingArmy / remainingNeighbors));
				
				for (AdvancedStar neighbor : neighbors)
				{
					try
					{
						success = success && orderMove(star.STAR, neighbor.STAR, eachGets, false);
					}
					catch (Exception e)
					{
						success = false;
					}
				}
			}

			//else: gene == 2. Disribute evenly:
			int remainingNeighbors = neighbors.size();
			int eachGets = (int) Math.round(Math.floor(star.STAR.getArmy() / remainingNeighbors));
			
			for (AdvancedStar neighbor : neighbors)
			{
				try
				{
					success = success && orderMove(star.STAR, neighbor.STAR, eachGets, false);
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
