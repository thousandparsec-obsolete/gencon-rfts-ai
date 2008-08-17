package gencon.robolib.RISK;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import net.thousandparsec.util.Pair;
import gencon.clientLib.RISK.ClientMethodsRISK;
import gencon.gamelib.RISK.gameobjects.Star;
import gencon.robolib.RISK.AdvancedMap.AdvancedStar;

public class ActionMethods 
{
	private final AdvancedMap MAP;
	private final ClientMethodsRISK CLIENT_RISK;
	//private final double DOUBLE_TOLERANCE = 1e-3;
	
	public ActionMethods(AdvancedMap advMap, ClientMethodsRISK clientRisk)
	{
		MAP = advMap;
		CLIENT_RISK = clientRisk;
	}
	
	/**
	 * Transfers troops from backwater planets (ones which are surrounded by friendlies),
	 * to nearby friendly planets, which are endangered. These orders are "top priority", 
	 * and will be placed on top of the order queue.
	 * 
	 * @param geneBackwaterDistribute Can be 0, 1 or 2. Determines the behavior of troop transfer: 
	 * 	0 means all troops to the most endangered star, 
	 * 	1 means 50% to most endangered with the rest evenly distributed between neighbors,
	 * 	and 2 evenly distributes between all neighbors.
	 * @return true if all went well, false if (at least some) orders failed. False indicates a bug!
	 */
	public boolean transferTroopsFromBackwaterStars(byte geneBackwaterDistribute, int myPlrNum)
	{
		assert geneBackwaterDistribute == 0 || geneBackwaterDistribute == 1 || geneBackwaterDistribute == 2;
	
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
			
			if (geneBackwaterDistribute == 0 || geneBackwaterDistribute == 1) //gives all troops to most endangered.
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
				
				if (geneBackwaterDistribute == 0) //give all to most endangered:
				{
					try
					{
						success = CLIENT_RISK.orderMove(star.STAR, endangered.STAR, star.STAR.getArmy() - 1, true);
						endangered.STAR.setArmy(endangered.STAR.getArmy() + star.STAR.getArmy() - 1);
					}
					catch (Exception e)
					{
						success = false;
					}
				}
				
				else // gene == 1. Give 50% to most threatened, then distribute evenly.
				{
					int halfArmy = (int) Math.round(Math.floor((star.STAR.getArmy() - 1) / 2));
					try
					{
						success = success && CLIENT_RISK.orderMove(star.STAR, endangered.STAR, halfArmy, false);
						endangered.STAR.setArmy(endangered.STAR.getArmy() + halfArmy);
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
							success = success && CLIENT_RISK.orderMove(star.STAR, neighbor.STAR, eachGets, false);
							neighbor.STAR.setArmy(neighbor.STAR.getArmy() + eachGets);
						}
						catch (Exception e)
						{
							success = false;
						}
					}
				}
			}
	
			else // gene == 2. Disribute evenly:
			{
				int remainingNeighbors = neighbors.size();
				int eachGets = (int) Math.round(Math.floor(star.STAR.getArmy() / remainingNeighbors));
				
				for (AdvancedStar neighbor : neighbors)
				{
					try
					{
						success = success && CLIENT_RISK.orderMove(star.STAR, neighbor.STAR, eachGets, false);
						neighbor.STAR.setArmy(neighbor.STAR.getArmy() + eachGets);
					}
					catch (Exception e)
					{
						success = false;
					}
				}
			}
			
		}
		
		return success;
	}

	/**
	 * Reinforces N-most-endangered planets, relative to the threat they face.
	 * 
	 * @param num_of_planets Number of planets to be reinforced.
	 * @param geneReinforce Can be 0, 1 or 2. Determines the amount of reinforcements to be distributed: 
	 * 	0 (33% of total available reinforcements), 1 (50%), or 2 (66%).
	 * @return true if all went well, false if (at least some) orders failed. False indicates a bug!
	 */
	public boolean ReinforceEndangeredPlanets(int num_of_stars, byte geneReinforce, int myPlrNum)
	{
		assert geneReinforce == 0 || geneReinforce == 1 || geneReinforce == 2;
		
		double importance = 0.0;
		if (geneReinforce == 0)
			importance = 0.33333;
		else if (geneReinforce == 1)
			importance = 0.5;
		else if (geneReinforce == 2)
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
					success = success && CLIENT_RISK.orderReinforce(star, reinforce, false);
					star.setArmy(star.getArmy() + reinforce);
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
	 * Commences a series of offensive actions. It will attack, until determined to be not beneficial according to the given parameters.
	 * 
	 * @param geneRisk Can be 0, 1 or 2. Determines the ratio of troops that needs to be established between my forces and enemy, to attack: 0 (+20%), 1 (+30%), 2 (+40%).
	 * @param geneCannonfodder Can be 0, 1 or 2. Determines the ratio of troops to be sent to battle from each star: 0 (50%), 1 (70%), 2 (%90).
	 * @return true if all went well, false if (at least some) orders failed. False indicates a bug!
	 */
	public boolean offensiveActions(byte geneRisk, byte geneCannonfodder, int myPlrNum)
	{
		assert (geneRisk == 0 || geneRisk == 1 || geneRisk == 2) && 
			(geneCannonfodder == 0 || geneCannonfodder == 1 || geneCannonfodder == 2); 
		
		//determining the risk factor:
		double risk = 0;
		if (geneRisk == 0)
			risk = 1.2;
		else if (geneRisk == 1)
			risk = 1.3;
		else if (geneRisk == 2)
			risk = 1.4;
		
		//determining the ratio of troops to be sent to combat:
		double cannonFodder = 0;
		if (cannonFodder == 0)
			cannonFodder = 0.5;
		else if (cannonFodder == 1)
			cannonFodder = 0.7;
		else if (cannonFodder == 2)
			cannonFodder = 0.9;
		
		boolean success = true; //to be returned.
		
		boolean stop = false; //continue until no further aggression is beneficial, according to params.
		do
		{
			Pair<Collection<AdvancedStar>, AdvancedStar> bestTarget = findBestTarget(myPlrNum);
			
			if (bestTarget == null) //nothing to attack!
				return true;
			
			int totalAttackers = 0;
			for (AdvancedStar myStar : bestTarget.left)
				totalAttackers += (int) Math.round(Math.floor(myStar.STAR.getArmy() * cannonFodder));
			
			int defenders = bestTarget.right.STAR.getArmy();
			
			double attackRatio = totalAttackers / defenders;
			
			if (attackRatio >= risk) //valid target!
				for (AdvancedStar attacker : bestTarget.left)
				{
					int troopsAttack = (int) Math.round(Math.floor(attacker.STAR.getArmy() * cannonFodder));
					
					if (troopsAttack > 0) //sometimes this will be the case! such is life.
					{
						try
						{
							success = success && CLIENT_RISK.orderMove(attacker.STAR, bestTarget.right.STAR, troopsAttack, false);
							attacker.STAR.setArmy(attacker.STAR.getArmy() - troopsAttack);
						}
						catch (Exception e)
						{
							success = false;
						}
					}
				}
				
			else // not beneficial anymore!
				stop = true;
			
		} while (!stop);
		
		return success;
	}
	
	/*
	 * @return Enemy star, most threatened by me, and the collection of friendly stars that surround it.
	 */
	private Pair<Collection<AdvancedStar>, AdvancedStar> findBestTarget(int myPlrNum)
	{
		//get all enemy advanced stars!
		Collection<AdvancedStar> enemyStars = MAP.getAllAdvStars();
		enemyStars.removeAll(MAP.getStarsOfPlayer(enemyStars, myPlrNum)); //remove mine.
		enemyStars.removeAll(MAP.getStarsOfPlayer(enemyStars, -1)); //remove neutral.
		
		//list of targets:
		List<Pair<Collection<AdvancedStar>, Pair<AdvancedStar, Integer>>> listOfTargets = new ArrayList<Pair<Collection<AdvancedStar>,Pair<AdvancedStar,Integer>>>();
		
		//populating list:
		for (AdvancedStar enemy : enemyStars)
		{
			//find my stars around it:
			Collection<Integer> myStarIds = enemy.STAR.getAdjacencies();
			Collection<AdvancedStar> myStars = new HashSet<AdvancedStar>();
			for (Integer i : myStarIds)
				myStars.add(MAP.getAdvancedStarWithId(i));
			
			//find if I can overpower it:
			int myStrength = 0;
			for (AdvancedStar myStar : myStars)
				myStrength += myStar.STAR.getArmy();
			int defenders = enemy.STAR.getArmy();
			int overpowerTheEnemy = defenders - myStrength;
			
			if (!myStars.isEmpty() && overpowerTheEnemy > 0) //weed out obviously bad options.
			{
				Pair<AdvancedStar, Integer> enemyPair = new Pair<AdvancedStar, Integer>(enemy, overpowerTheEnemy);
				Pair<Collection<AdvancedStar>, Pair<AdvancedStar, Integer>> listPair = new Pair<Collection<AdvancedStar>, Pair<AdvancedStar,Integer>>(myStars, enemyPair);
				listOfTargets.add(listPair);
			}
		}
		
		//find best!
		Iterator<Pair<Collection<AdvancedStar>, Pair<AdvancedStar, Integer>>> iterator = listOfTargets.iterator();
		
		if (iterator.hasNext()) //if there are candidates.
		{
			Pair<Collection<AdvancedStar>, Pair<AdvancedStar, Integer>> bestCandidate = iterator.next();
			while (iterator.hasNext())
			{
				Pair<Collection<AdvancedStar>, Pair<AdvancedStar, Integer>> candidate = iterator.next();
				if (candidate.right.right > bestCandidate.right.right)
					bestCandidate = candidate;
			}
			
			//found best target!
			return new Pair<Collection<AdvancedStar>, AdvancedStar>(bestCandidate.left, bestCandidate.right.left);
		}
		
		//else: if no enemies nearby!
		return null;
	}
}
