package gencon.robolib.RISK;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import net.thousandparsec.netlib.TPException;
import net.thousandparsec.util.Pair;

import gencon.clientLib.RISK.ClientMethodsRISK;
import gencon.gamelib.RISK.UniverseMap;
import gencon.gamelib.RISK.gameobjects.Star;
import gencon.robolib.RISK.AdvancedMap.AdvancedStar;
import gencon.utils.DebugOut;

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
	private final DebugOut out;
	//private final double DOUBLE_TOLERANCE = 1e-3;
	
	//a collection of stars, which have been backed-up by transfer from backwaters. To use when deciding to evacuate.
	private Collection<Pair<AdvancedStar, Pair<AdvancedStar, Integer>>> possiblyTransferFromBackwaters;
	//a collection of stars being possiblyReinforce/moved-to each turn. To use when deciding to evacuate.
	private Collection<Pair<AdvancedStar, Integer>> possiblyReinforce;
	//a collection of stars, which participate in attack. For use when deciding to evacuating.
	private Collection<AdvancedStar> attackingStars;
	//a collection of stars, which have been evacuated. Not to send reinforcements there!
	private Collection<AdvancedStar> evacuated;
	
	public ActionMethods(AdvancedMap advMap, ClientMethodsRISK clientRisk, DebugOut output)
	{
		out = output;
		MAP = advMap;
		CLIENT_RISK = clientRisk;
	}
	
	public void incrementTurn(UniverseMap newMap, int myPlrId)
	{
		possiblyTransferFromBackwaters = new HashSet<Pair<AdvancedStar,Pair<AdvancedStar,Integer>>>();
		possiblyReinforce = new HashSet<Pair<AdvancedStar,Integer>>();
		attackingStars = new HashSet<AdvancedStar>();
		evacuated = new HashSet<AdvancedStar>();
		MAP.updateMap(newMap, myPlrId);
	}
	
	/**
	 * BEST IF ACTED ON FIRST.
	 * 
	 * Transfers troops from backwater planets (ones which are surrounded by friendlies),
	 * to nearby friendly planets, which are endangered.
	 * 
	 * @param geneBackwaterDistribute Can be 0, 1 or 2. Determines the behavior of troop transfer: 
	 * 	0 sends all troops to the most endangered star, 
	 * 	1 sends 50% to most endangered, with the rest evenly distributed between neighbors,
	 * 	and 2 evenly distributes between all neighbors.
	 */
	public void transferTroopsFromBackwaterStars(byte geneBackwaterDistribute, int myPlrNum) throws IOException
	{
		assert geneBackwaterDistribute == 0 || geneBackwaterDistribute == 1 || geneBackwaterDistribute == 2;
	
		//get all my backwaters, with army > 1
		Collection<AdvancedStar> backwaters = MAP.getAllBackwaters(myPlrNum);
		Collection<AdvancedStar> armyOfOne = new HashSet<AdvancedStar>();
		for (AdvancedStar as : backwaters)
			if (as.STAR.getArmy() == 1)
				armyOfOne.add(as);
		backwaters.removeAll(armyOfOne);
		
		for (AdvancedStar star : backwaters)
		{
			//collect all friendly neighbors, which aren't backwater themselves.
			Collection<AdvancedStar> neighbors = MAP.getNeighbors(star);
			Collection<AdvancedStar> invalidNeighbors = new HashSet<AdvancedStar>();
			for (AdvancedStar as : neighbors)
				if (as.getBackwaters() == true)
					invalidNeighbors.add(as);
			neighbors.removeAll(invalidNeighbors);
			
			if (!neighbors.isEmpty()) //if it's relevant at all.
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
				
				int army = star.STAR.getArmy();
				int giveMostEndangered = 0;
				
				if (geneBackwaterDistribute == 0) //give all to most endangered
				{
					giveMostEndangered = army;
					army = 0;
				}
				else if (geneBackwaterDistribute == 1) //give half to most endangered
				{
					giveMostEndangered = (int) Math.round((Math.floor(army / 2)));
					army -= giveMostEndangered;
				}
				
				//give something to the most endangered, if dictated by parameters.
				if (giveMostEndangered > 0 && (geneBackwaterDistribute == 0 || geneBackwaterDistribute == 1)) 
				{
					star.STAR.setArmy(star.STAR.getArmy() - giveMostEndangered);
					possiblyTransferFromBackwaters.add(new Pair<AdvancedStar, Pair<AdvancedStar,Integer>>(star, new Pair<AdvancedStar,Integer>(endangered, giveMostEndangered)));
				}
				
				//if any left, distribute evenly:
				int eachGets = (int) Math.round(Math.floor((double)army / neighbors.size()));
	
				//distribute by portions larger than 1:
				if (eachGets > 1)
					for (AdvancedStar neighbor : neighbors)
					{
						star.STAR.setArmy(star.STAR.getArmy() - eachGets);
						army--;
						possiblyTransferFromBackwaters.add(new Pair<AdvancedStar, Pair<AdvancedStar,Integer>>(star, new Pair<AdvancedStar,Integer>(neighbor, eachGets)));
					}
				
				
				//distribute by 1 any troops that are left:
				while (army > 0)
					for (AdvancedStar neighbor : neighbors)
					{
						star.STAR.setArmy(star.STAR.getArmy() - 1);
						army--;
						possiblyTransferFromBackwaters.add(new Pair<AdvancedStar, Pair<AdvancedStar,Integer>>(star, new Pair<AdvancedStar,Integer>(neighbor, 1)));
			
						if (army == 0)
							break;
					}
			}
		}
	}

	
	/**
	 * Reinforces N-most-endangered planets, relative to the threat they face.
	 * 
	 * @param geneDefence Can be 0, 1 or 2. Determines the maximum amount of stars possiblyReinforce:
	 * 	0 (max helped: 3), 1 (max helped: 5), 2 (max helped: 7). Will help only those at risk.
	 * @param geneReinforce Can be 0, 1 or 2. Determines the amount of reinforcements to be distributed: 
	 * 	0 (50% of total available reinforcements), 1 (70%), or 2 (100%).
	 */
	public void reinforceEndangeredStars(byte geneDefence, byte geneReinforce, int myPlrNum) throws IOException
	{
		assert (geneReinforce == 0 || geneReinforce == 1 || geneReinforce == 2) &&
			(geneDefence == 0 || geneDefence == 1 || geneDefence == 2);
		
		double importance = 0.0;
		if (geneReinforce == 0)
			importance = 0.5;
		else if (geneReinforce == 1)
			importance = 0.7;
		else
			importance = 1.0;
		
		int maxReinforcedStars = 0;
		if (geneDefence == 0)
			maxReinforcedStars = 3;
		else if (geneDefence == 1)
			maxReinforcedStars = 5;
		else
			maxReinforcedStars = 7;
		
		//determine how much of the total should be spent overall:
		int totalReinforcements = (int) Math.round(Math.floor(MAP.getBasicMap().getMyReinforcements() * importance));
		
		if (totalReinforcements == 0)
			return; //nothing to be done, but nothing went wrong!
		
		//get list of stars by threat:
		Collection<AdvancedStar> myStars = MAP.getStarsOfPlayer(MAP.getAllAdvStars(), myPlrNum);
		List<AdvancedStar> riskList = MAP.sortByThreat(myStars);
		
		//remove the ones not threatened:
		for (int i = 0; i < riskList.size(); i++)
			if (riskList.get(i).getThreat() <= 1)
				riskList.remove(i);
		
		//to record the actual reinforcements (may differ due to the 'floor' function)
		int actualTotalReinforced = 0;
		
		//allocate the reinforcements, and send the orders!
		for (int i = 0; i < riskList.size() && i < maxReinforcedStars && totalReinforcements > 0; i++)
		{
			AdvancedStar threatened = riskList.get(i);
			//my troops:
			int myTroops = threatened.STAR.getArmy();
			
			int enemyTroops = (int) Math.round(Math.floor(threatened.getThreat() * myTroops)); //true if it's 1 vs. 1.
			
			//get the amount to be possiblyReinforce:
			int reinforce = 0;
			while (totalReinforcements > 0 && (myTroops + reinforce) < enemyTroops)
			{
				reinforce++;
				totalReinforcements--;
			}
			
			if (reinforce > 0)
			{			
				actualTotalReinforced += reinforce;
				possiblyReinforce.add(new Pair<AdvancedStar, Integer>(threatened, reinforce));
			}
		}
		
		//adjusting available reinforcements in the map:
		MAP.getBasicMap().setMyReinforcements(MAP.getBasicMap().getMyReinforcements() - actualTotalReinforced);
		assert MAP.getBasicMap().getMyReinforcements() >= 0;
	}
	
	/**
	 * Evenly distributes a portion of the remaining reinforcements, as directed by geneCheapness.
	 * 
	 * @param geneCheapness Can be 0, 1 or 2. Determines what percentage of available reinforcements to distribute amongst owned stars.
	 * 	0 (50%) , 1 (75%) , 2 (100%)
	 */
	public void distributeRemainingReinforcements(byte geneCheapness, int myPlrNum) throws IOException
	{
		int reinforcements = MAP.getBasicMap().getMyReinforcements();
		
		//setting the actual value:
		if (geneCheapness == 0)
			reinforcements = (int)Math.round(Math.floor((double)reinforcements / 2.0));
		else if (geneCheapness == 1)
			reinforcements = (int)Math.round(Math.floor(3.0 * (double)reinforcements / 4.0));
		//else: nothing happens.
		
		if (reinforcements == 0) //can't do anything!
			return;
		
		Collection<AdvancedStar> allStars = MAP.getAllAdvStars();
		Collection<AdvancedStar> myStars = MAP.getStarsOfPlayer(allStars, myPlrNum);
		
		//don't count backwater stars!
		Collection<AdvancedStar> backwaters = new HashSet<AdvancedStar>();
		for (AdvancedStar as : myStars)
			if (as.getBackwaters())
				backwaters.add(as);
		myStars.removeAll(backwaters);
	
		if (myStars.size() == 0) //there's only backwater; I won!
			return;
		
		int actualReinforced = 0; //the actual number of reinforcements issued.
		
		//try to divide reinforcements evenly:
		int reinforceEach = (int) Math.round(Math.floor((double)reinforcements / myStars.size()));
		if (reinforceEach > 0)
			for (AdvancedStar s : myStars)
			{
				actualReinforced += reinforceEach;
				reinforcements -= reinforceEach;
				possiblyReinforce.add(new Pair<AdvancedStar, Integer>(s, reinforceEach));
					
				if (reinforcements == 0)
					break;
			}
		
		//distribute rest:
		while (reinforcements > 0)
			for (AdvancedStar s : myStars)
			{
				actualReinforced ++;
				reinforcements --;
				possiblyReinforce.add(new Pair<AdvancedStar, Integer>(s, 1));
					
				if (reinforcements == 0)
					break;
			}
		
		MAP.getBasicMap().setMyReinforcements(reinforcements - actualReinforced);
		assert MAP.getBasicMap().getMyReinforcements() >= 0;
		
	}
	
	
	
	
	/**
	 * Commences a series of offensive actions. For each owned star, it will see if it's beneficial to attack, according to the given genetic parameters.
	 * 
	 * @param geneBravery Can be 0, 1 or 2. Determines the ratio of troops that needs to be established between my forces and enemy, to attack: 0 (+10%), 1 (+25%), 2 (+40%).
	 * @param geneCannonfodder Can be 0, 1 or 2. Determines the ratio of troops to be sent to battle from each star: 0 (70%), 1 (85%), 2 (%100).
	 */
	public void offensiveActions(byte geneBravery, byte geneCannonfodder, int myPlrNum) throws IOException
	{
		assert (geneBravery == 0 || geneBravery == 1 || geneBravery == 2) && 
			(geneCannonfodder == 0 || geneCannonfodder == 1 || geneCannonfodder == 2); 
		
		//determining the risk factor:
		double overpowerBy = 0;
		if (geneBravery == 0)
			overpowerBy = 1.1;
		else if (geneBravery == 1)
			overpowerBy = 1.25;
		else
			overpowerBy = 1.4;
		
		//determining the ratio of troops to be sent to combat:
		double cannonFodder = 0.0;
		if (geneCannonfodder == 0)
			cannonFodder = 0.7;
		else if (geneCannonfodder == 1)
			cannonFodder = 0.85;
		else
			cannonFodder = 1.0;
		
		//getting all my stars, with army > 1:
		Collection<AdvancedStar> allStars = MAP.getAllAdvStars();
		Collection<AdvancedStar> allMyStars = MAP.getStarsOfPlayer(allStars, myPlrNum);
		Collection<AdvancedStar> armyOfOne = new HashSet<AdvancedStar>();
		for (AdvancedStar as : allMyStars)
			if (as.STAR.getArmy() == 1)
				armyOfOne.add(as);
		allMyStars.removeAll(armyOfOne);
		
		//keeping track of who I attack:
		Collection<Pair<AdvancedStar, Integer>> attacked = new HashSet<Pair<AdvancedStar,Integer>>();

		//iterate for each star I own THRICE, see if it can attack. Reason: coordinating attacks together. 
		//The catalyst for attack must be a star with attackers > defenders, but the rest can join in the fun.
		int count = 0;
		while (count < 3)
		{
			count++;
			for (AdvancedStar myStar : allMyStars)
			{
				int attackers = (int) Math.round(Math.floor(myStar.STAR.getArmy()) * cannonFodder);
				
				if (myStar.STAR.getArmy() == attackers)
					attackers --;
				
				//finding enemies
				Collection<AdvancedStar> neighbors = MAP.getNeighbors(myStar);
				Collection<AdvancedStar> enemies = new HashSet<AdvancedStar>();
				for (AdvancedStar neighbor : neighbors)
					if (neighbor.STAR.getOwner() != myPlrNum && neighbor.STAR.getOwner() != -1)
						enemies.add(neighbor);
				
				//finding weakest enemy it can possibly attack!
				AdvancedStar target = null;
				for (AdvancedStar enemy : enemies)
				{
					//seeing if it was previously attacked as well:
					int alreadyAttackedBy = 0;
					for (Pair<AdvancedStar, Integer> pair : attacked)
						if (pair.left.STAR.GAME_ID == enemy.STAR.GAME_ID)
							alreadyAttackedBy += pair.right.intValue();
					
					int defenders = enemy.STAR.getArmy();
					double ratio = (double)(attackers + alreadyAttackedBy) / defenders;
					if (ratio >= overpowerBy && (target == null || defenders < target.STAR.getArmy()))
						target = enemy;
				}
				
				
				if (target != null && myStar.STAR.getArmy() - attackers >= 1 && attackers > 0) //if it's there, and it's valid!
				{
					try
					{
						boolean done = CLIENT_RISK.orderMove(myStar.STAR, target.STAR, attackers, false);
						out.pl("Attacking <" + target.STAR.GAME_ID + "> (Army: " + target.STAR.getArmy() + ") from <" + myStar.STAR.GAME_ID + "> (Army: " + myStar.STAR.getArmy() + ") with " + attackers + " troops; Sever said '" + done + "'");
						if (done)
						{
							myStar.STAR.setArmy(myStar.STAR.getArmy() - attackers);
							attackingStars.add(myStar);
						}
					}
					catch (TPException e)
					{
						out.pl("<Illegal action: attack>");
					}
				}
				
				
			}
		}

	}
	
	/**
	 * Governs expansion to nearby neutral planets. From the lowest-threat friendly stars, to their lowest-threat neutral neighbors.
	 * 
	 * @param geneExpansionism Can be 0, 1 or 2. Determines the maximum number of stars to be colonized at a turn.
	 * 	0 (max. of 4), 1 (max. of 7), 2 (max. of 10).
	 * @param geneEmigration Can be 0, 1 or 2. Determines the ratio of troops to be dispatched.
	 * 	0 (40% of army), 1 (60%), 2 (80%).
	 * @param myPlrNum
	 */
	public void expandToNeutralStars(byte geneExpansionism, byte geneEmigration, int myPlrNum) throws IOException
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
			emigration = 0.4;
		else if (geneEmigration == 1)
			emigration = 0.6;
		else
			emigration = 0.8;
		
		//get the friendly stars, remove unfit to colonize, and sort them by threat:
		Collection<AdvancedStar> allStars = MAP.getAllAdvStars();
		Collection<AdvancedStar> friendly = MAP.getStarsOfPlayer(allStars, myPlrNum);
		
		//weeding out ones with army == 1: (unfit to move their units)
		Collection<AdvancedStar> unfit = new HashSet<AdvancedStar>();
		for (AdvancedStar as : friendly)
			if (as.STAR.getArmy() == 1)
				unfit.add(as);
		friendly.removeAll(unfit);
		//sorting:
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
			if (possibleColonist.getThreat() <= possibleColonist.STAR.getArmy() - colonists && colonists > 0)
			{
				//get all neutral neighbors:
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
				if (safestFutureColony != null && safestFutureColony.getThreat() <= colonists)
				{
					try
					{
						boolean done = CLIENT_RISK.orderMove(possibleColonist.STAR, safestFutureColony.STAR, colonists, false);
						out.pl("Moving to neutral star: <" + safestFutureColony.STAR.GAME_ID + ">  From <" + possibleColonist.STAR.GAME_ID + "> (Army: " + possibleColonist.STAR.getArmy() + ") with " + colonists + " troops; Sever said '" + done + "'");
						if (done)
							possibleColonist.STAR.setArmy(possibleColonist.STAR.getArmy() - colonists);
					}
					catch (TPException e)
					{
						out.pl("<Illegal action: moving to neutral>");
					}
				}
			}
		}
		
	}
	
	
	/**
	 * SHOULD BE THE LAST ACTION UNDERTAKEN, AFTER ALL ELSE FAILED.
	 * 
	 * All planets under "grave threat" (as specified by geneCowardice) look if they can evacuate their troops to a safer place. 1 unit will remain.
	 * 
	 * @param geneStoicism Can be 0, 1 or 2. Determines the threshold of threat under which my forces need to escape to a safer location.
	 * 	0 (outnumbered by 10%), 1 (20%), 2 (30%).
	 */
	public void evacuateToSafety(byte geneStoicism, int myPlrNum) throws IOException
	{
		assert geneStoicism == 0 || geneStoicism == 1 || geneStoicism == 2;
		
		//determine the coefficient, which decides on action:
		double overrun = 0.0;
		if (geneStoicism == 0)
			overrun = 1.5;
		else if (geneStoicism == 1)
			overrun = 2.0;
		else
			overrun = 3.0;
		
		//get my stars:
		Collection<AdvancedStar> allStars = MAP.getAllAdvStars();
		Collection<AdvancedStar> myStars = MAP.getStarsOfPlayer(allStars, myPlrNum);

		for (AdvancedStar myStar : myStars)
		{
			//search for the star in the possiblyReinforce, and adjust the threat formula:
			int reinforcedBy = 0;
			for (Pair<AdvancedStar, Integer> pair : possiblyReinforce)
				if (pair.left.STAR.GAME_ID == myStar.STAR.GAME_ID)
					reinforcedBy += pair.right.intValue();
			for (Pair<AdvancedStar, Pair<AdvancedStar,Integer>> pair : possiblyTransferFromBackwaters)
				if (pair.right.left.STAR.GAME_ID == myStar.STAR.GAME_ID)
					reinforcedBy += pair.right.right.intValue();
			
			//equation works for 1 enemy player. Extra-safe for > 1 types of enemies.
			double threatAfterReinforce = (myStar.getThreat() * myStar.STAR.getArmy()) / (myStar.STAR.getArmy() + reinforcedBy);
			
			//taking into account whether the star is attacking presently: (cut the threat by 1/4)
			if (attackingStars.contains(myStar))
				threatAfterReinforce = threatAfterReinforce / 3;
			
			if (threatAfterReinforce > overrun && myStar.STAR.getArmy() > 1) //RUN, FOREST!
			{
				//collect all neighbors:
				Collection<AdvancedStar> neighbors = MAP.getNeighbors(myStar);
				
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
				if (sanctuary.getThreat() < myStar.getThreat() 
						&& (sanctuary.STAR.getOwner() == myPlrNum || sanctuary.STAR.getArmy() < myStar.STAR.getArmy() - 1))
				{
					try
					{
						boolean done = CLIENT_RISK.orderMove(myStar.STAR, sanctuary.STAR, myStar.STAR.getArmy() - 1, false);
						out.pl("Evacuating from <" + myStar.STAR.GAME_ID + "> (Army: " + myStar.STAR.getArmy() + ") to <" + sanctuary.STAR.GAME_ID + "> , with " + (myStar.STAR.getArmy() - 1) + " troops; Threat after all considerations: " + threatAfterReinforce + "; Sever said '" + done + "'");
						if (done)
						{
							myStar.STAR.setArmy(1); //simple!
							evacuated.add(myStar);
						}
					}
					catch (TPException e)
					{
						out.pl("<Illegal action: evacuation>");
					}
				}
			}
		}
		
	}
	
	
	public void actuallyReinforceStars() throws IOException
	{
		for (Pair<AdvancedStar, Integer> pair : possiblyReinforce)
			if (!evacuated.contains(pair.left)) //if it's not evacuated!
			{
				try
				{
					Star star = pair.left.STAR;
					int reinforce = pair.right.intValue();
					boolean done = CLIENT_RISK.orderReinforce(star, reinforce, false);
					out.pl("Reinforcing <" + star.GAME_ID + "> with " + reinforce + " troops; Sever said '" + done + "'");
				}
				catch (TPException e)
				{
					out.pl("<Illegal action: reinforce>");
				}
			}
	}
	
	public void actuallyTransferFromBackwaters()
	{
		for (Pair<AdvancedStar, Pair<AdvancedStar, Integer>> pair : possiblyTransferFromBackwaters)
			if (!evacuated.contains(pair.right.left))
			{
				AdvancedStar star = pair.left;
				AdvancedStar to = pair.right.left;
				int troops = pair.right.right;
				try
				{
					boolean done = CLIENT_RISK.orderMove(star.STAR, to.STAR, troops, false);
					out.pl("Transfering " + troops +  " troops from backwaters <" + star.STAR.GAME_ID + "> to <" + to.STAR.GAME_ID + ">; Sever said '" + done + "'");
				}
				catch (Exception e)
				{
					out.pl("<Illegal action: transfer from backwaters>");
				}
			}
			
			
	}
}
