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

/**
 * A class, which contains methods that govern the behavior of the bot, 
 * according to certain parameters (its 'genotype').
 * 
 * @author Victor Ivri
 */
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
	 * SHOULD BE USED FIRST.
	 * 
	 * Transfers troops from backwater planets (ones which are surrounded by friendlies),
	 * to nearby friendly planets, which are endangered.
	 * 
	 * @param geneBackwaterDistribute Can be 0, 1 or 2. Determines the behavior of troop transfer: 
	 * 	0 sends all troops to the most endangered star, 
	 * 	1 sends 50% to most endangered, with the rest evenly distributed between neighbors,
	 * 	and 2 evenly distributes between all neighbors.
	 * @return true if all went well, false if (at least some) orders failed. False indicates a bug or problem in connection!
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
						success = CLIENT_RISK.orderMove(star.STAR, endangered.STAR, star.STAR.getArmy() - 1, false);
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
	 * @param geneDefence Can be 0, 1 or 2. Determines the threshold of risk for sending reinforcements:
	 * 	0 (max helped: 3), 1 (max helped: 5), 2 (max helped: 7). Will help only those at risk.
	 * @param geneReinforce Can be 0, 1 or 2. Determines the amount of reinforcements to be distributed: 
	 * 	0 (33% of total available reinforcements), 1 (66%), or 2 (99%).
	 * @return true if all went well, false if (at least some) orders failed. False indicates a bug or problem in connection!
	 */
	public boolean ReinforceEndangeredPlanets(byte geneDefence, byte geneReinforce, int myPlrNum)
	{
		assert (geneReinforce == 0 || geneReinforce == 1 || geneReinforce == 2) &&
			(geneDefence == 0 || geneDefence == 1 || geneDefence == 2);
		
		double importance = 0.0;
		if (geneReinforce == 0)
			importance = 1 / 3;
		else if (geneReinforce == 1)
			importance = 2 / 3;
		else
			importance = 0.999999999;
		
		int maxReinforcedStars = 0;
		if (geneDefence == 0)
			maxReinforcedStars = 3;
		else if (geneDefence == 1)
			maxReinforcedStars = 5;
		else
			maxReinforcedStars = 7;
		
		//get list of stars by threats:
		Collection<AdvancedStar> myStars = MAP.getStarsOfPlayer(MAP.getAllAdvStars(), myPlrNum);
		List<AdvancedStar> riskList = MAP.sortByThreat(myStars);
		
		/*
		 * calculate actual number of stars to be helped. 
		 * A star will only be helped if the threat on it is > 0.
		 * Maximum to be helped = maxReinforcedStars.
		 */		
		int num_of_stars = 0;
		for (int i = 0; i < maxReinforcedStars; i++)
		{
			if (riskList.get(i).getThreat() > 0)
				num_of_stars ++;
			else
				break;
		}
		
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
	 * @param geneBravery Can be 0, 1 or 2. Determines the ratio of troops that needs to be established between my forces and enemy, to attack: 0 (+20%), 1 (+30%), 2 (+40%).
	 * @param geneCannonfodder Can be 0, 1 or 2. Determines the ratio of troops to be sent to battle from each star: 0 (50%), 1 (70%), 2 (%90).
	 * @return true if all went well, false if (at least some) orders failed. False indicates a bug or problem in connection!
	 */
	public boolean offensiveActions(byte geneBravery, byte geneCannonfodder, int myPlrNum)
	{
		assert (geneBravery == 0 || geneBravery == 1 || geneBravery == 2) && 
			(geneCannonfodder == 0 || geneCannonfodder == 1 || geneCannonfodder == 2); 
		
		//determining the risk factor:
		double risk = 0;
		if (geneBravery == 0)
			risk = 1.2;
		else if (geneBravery == 1)
			risk = 1.3;
		else
			risk = 1.4;
		
		//determining the ratio of troops to be sent to combat:
		double cannonFodder = 0;
		if (cannonFodder == 0)
			cannonFodder = 0.5;
		else if (cannonFodder == 1)
			cannonFodder = 0.7;
		else
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
		
		//find best target!
		Iterator<Pair<Collection<AdvancedStar>, Pair<AdvancedStar, Integer>>> iterator = listOfTargets.iterator();
		
		if (!iterator.hasNext()) //if no enemies nearby!
			return null;
			
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
	
	
	/**
	 * Governs expansion to nearby neutral planets. From the lowest-threat friendly stars, to their lowest-threat neutral neighbors.
	 * 
	 * @param geneExpansionism Can be 0, 1 or 2. Determines the maximum number of stars to be colonized at a turn.
	 * 	0 (max. of 4), 1 (max. of 7), 2 (max. of 10).
	 * @param geneEmigration Can be 0, 1 or 2. Determines the ratio of troops to be dispatched.
	 * 	0 (20% of army), 1 (40%), 2 (60%).
	 * @param myPlrNum
	 * @return true if all went well, false if (at least some) orders failed. False indicates a bug or problem in connection!
	 */
	public boolean expandToNeutralStars(byte geneExpansionism, byte geneEmigration, int myPlrNum)
	{
		assert (geneExpansionism == 0 || geneExpansionism == 1 || geneExpansionism == 2) && 
			(geneEmigration == 0 || geneEmigration == 1 || geneEmigration == 2);  
		
		int maxColonies = 0;
		if (geneExpansionism == 0)
			maxColonies = 4;
		else if (geneExpansionism == 1)
			maxColonies = 7;
		else
			maxColonies = 10;
		
		double emigration = 0.0;
		if (geneEmigration == 0)
			emigration = 0.2;
		else if (geneEmigration == 1)
			emigration = 0.4;
		else
			emigration = 0.6;
		
		boolean success = true; //to be returned.
		
		//sort the friendly stars by threat:
		Collection<AdvancedStar> allStars = MAP.getAllAdvStars();
		Collection<AdvancedStar> friendly = MAP.getStarsOfPlayer(allStars, myPlrNum);
		List<AdvancedStar> friendlyByThreat = MAP.sortByThreat(friendly);
		
		//setting the actual number of maximum possible colonies:
		if (maxColonies > friendlyByThreat.size())
			maxColonies = friendlyByThreat.size();
		
		//iterate on friendly stars, lowest threat first, for 'maxColonies' times.
		for (int i = friendlyByThreat.size() - 1; i > friendlyByThreat.size() - 1 - maxColonies; i--)
		{
			AdvancedStar possibleColonist = friendlyByThreat.get(i);
			int colonists = (int) Math.round(Math.floor(possibleColonist.STAR.getArmy() * emigration));
			
			//see if it'll be safe and viable to colonize!
			if (possibleColonist.getThreat() < possibleColonist.STAR.getArmy() - colonists && colonists > 0)
			{
				//get all neutral neighbors, and sort them by threat:
				Collection<Integer> neighborIds = possibleColonist.STAR.getAdjacencies();
				Collection<AdvancedStar> neutralNeighbors = new HashSet<AdvancedStar>();
				for (Integer id : neighborIds)
				{
					AdvancedStar neighbor = MAP.getAdvancedStarWithId(id);
					if (neighbor.STAR.getOwner() == -1)
						neutralNeighbors.add(neighbor);
				}
				
				//find the safest place to colonize, and see if it's safe enough:
				AdvancedStar safestFutureColony = null;
				for (AdvancedStar possibleColony : neutralNeighbors)
					if (safestFutureColony == null || possibleColony.getThreat() < safestFutureColony.getThreat())
						safestFutureColony = possibleColony;
				
				//if this place exists, and it's safe enough.
				if (safestFutureColony != null && safestFutureColony.getThreat() < colonists)
				{
					try
					{
						success = success && CLIENT_RISK.orderMove(possibleColonist.STAR, safestFutureColony.STAR, colonists, false);
						possibleColonist.STAR.setArmy(possibleColonist.STAR.getArmy() - colonists);
						safestFutureColony.STAR.setOwner(myPlrNum);
						safestFutureColony.STAR.setArmy(colonists);
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
	 * SHOULD BE THE LAST ACTION UNDERTAKEN, AFTER ALL ELSE FAILED.
	 * 
	 * All planets under "grave threat" (as specified by geneCowardice) look if they can evacuate their troops to a safer place. 1 unit will remain.
	 * 
	 * @param geneStoicism Can be 0, 1 or 2. Determines the threshold of threat under which my forces need to escape to a safer location.
	 * 	0 means being outnumbered by 10%, 1 (20%), 2 (30%).
	 * @return true if all went well, false if (at least some) orders failed. False indicates a bug or problem in connection!
	 */
	public boolean evacuateToSafety(byte geneStoicism, int myPlrNum)
	{
		assert geneStoicism == 0 || geneStoicism == 1 || geneStoicism == 2;
		
		//determine the coefficient, which decides on action:
		double overrun = 0.0;
		if (geneStoicism == 0)
			overrun = 0.1;
		else if (geneStoicism == 1)
			overrun = 0.2;
		else
			overrun = 0.3;
		
		boolean success = true; //to be returned.
		
		//get my stars:
		Collection<AdvancedStar> allStars = MAP.getAllAdvStars();
		Collection<AdvancedStar> myStars = MAP.getStarsOfPlayer(allStars, myPlrNum);

		for (AdvancedStar myStar : myStars)
			if (myStar.getThreat() / myStar.STAR.getArmy() > overrun) //RUN, FOREST!
			{
				//collect all neighbors:
				Collection<Integer> neighborIds = myStar.STAR.getAdjacencies();
				Collection<AdvancedStar> neighbors = new HashSet<AdvancedStar>();
				for (Integer i : neighborIds)
					neighbors.add(MAP.getAdvancedStarWithId(i));
				
				//get the lowest-threat star:
				Iterator<AdvancedStar> iterator = neighbors.iterator();
				AdvancedStar sanctuary = iterator.next();
				while (iterator.hasNext())
				{
					AdvancedStar possibleEscape = iterator.next();
					if (possibleEscape.getThreat() < sanctuary.getThreat())
						sanctuary = possibleEscape;
				}
				
				//see if there's any point:
				if (sanctuary.getThreat() < myStar.getThreat())
				{
					try
					{
						success = success && CLIENT_RISK.orderMove(myStar.STAR, sanctuary.STAR, myStar.STAR.getArmy() - 1, false);
						myStar.STAR.setArmy(1); //simple!
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
