package gencon.robolib;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import gencon.Master;
import gencon.evolutionlib.Genotype;
import gencon.gamelib.RFTS.gameobjects.Fleet;
import gencon.gamelib.RFTS.gameobjects.Planet;
import gencon.gamelib.RFTS.gameobjects.StarSystem;
import gencon.robolib.RFTS.ActionMethods;
import gencon.robolib.RFTS.AdvancedMap;
import gencon.robolib.RFTS.HigherLevelActions;
import gencon.robolib.RFTS.AdvancedMap.Sectors;
import gencon.robolib.RFTS.AdvancedMap.Sectors.Sector;

/**
 * The AI robot, which will play the game.
 * 
 * @author Victor Ivri
 * 
 */
public class Robot 
{
	public enum TURN_TYPE
	{
		PRODUCTION, MOVEMENT, BUILD_FLEET;
	}
	
	/*
	 * The behavioral characteristics of the robot, defined by a {@link Genotype}. 
	 */
	private final Phenotype CHARACTER;
	

	public final Master MASTER;
	
	public final short DIFFICULTY;
	
	private final HigherLevelActions ACTIONS;
	
	public static final byte WORK_TIME = 10; //The time, in seconds, required to complete a turn. 
	//if remaining time in some turn is less than that, robot will not execute until next turn.
	//ONLY AN ESTIMATE NOW!!! NEED TO CALCULATE ACTUAL TIMES! (MAY DEPEND ON PING).
	
	private short turn_num = 0; //the current turn number; starts at 0.
	private TURN_TYPE turnType;

	
	public Robot(Master master) throws Exception
	{
		MASTER = master;
		CHARACTER = new Phenotype(new Genotype(MASTER.CLIENT.getGenomeFileClasspath()));
		DIFFICULTY = MASTER.CLIENT.getDifficulty();
		ACTIONS = new HigherLevelActions(new ActionMethods(MASTER.CLIENT, MASTER.GAME_STATUS));
	}
	
	/*
	 * Gets the type of RFTS turn from the turn number.
	 */
	private void determineTurn()
	{
		int turn = MASTER.getTurn();
		byte convert = (byte)(turn % 3);
		
		switch (convert)
		{
			case (0): turnType = TURN_TYPE.PRODUCTION;
			case (1): turnType = TURN_TYPE.MOVEMENT;
			case (2): turnType = TURN_TYPE.BUILD_FLEET;
		}
	}
	
	
	public void startTurn(int time_remaining)
	{
		CHARACTER.updatePhenotype(turn_num);
		
		/*
		 * 1) evaluate situation.
		 * 2) act on it!
		 */
		
		//test();
		//proofOfConcept();
	}
	
	private void incrementTurn()
	{
		turn_num ++; //incrementing the subjective turn count.
		
		switch (turnType) //setting the turn type.
		{
			case PRODUCTION: turnType = TURN_TYPE.MOVEMENT;
			case MOVEMENT: turnType = TURN_TYPE.BUILD_FLEET;
			case BUILD_FLEET: turnType = TURN_TYPE.PRODUCTION;
		}
	}
	
	//A TEST METHOD:
	private void test()
	{
		Collection<Sector> sectors = ACTIONS.ACT.MAP.SECTORS.SECTORS;
		Collection<Planet> myPlanets = new HashSet<Planet>();
		Collection<Fleet> myFleet = new HashSet<Fleet>();
		
		for (Sector sc : sectors)
		{
			MASTER.pr("Sector " + sc.ID + " contains: ");
			for (Integer i : sc.getContents())
				MASTER.pr(" " + ACTIONS.ACT.MAP.getBasicMap().getById(i.intValue()).NAME);
			MASTER.pl("");
			
			myPlanets.addAll(ACTIONS.ACT.MAP.getMyPlanets(sc));
			myFleet.addAll(ACTIONS.ACT.MAP.getAllMyFleet(sc));
		}
		
		Iterator<Fleet> fleetIterator = myFleet.iterator();
		Fleet fl = fleetIterator.next();
		
		Iterator<Planet> plIt = myPlanets.iterator();
		Planet pl = plIt.next();
		StarSystem ss = (StarSystem)ACTIONS.ACT.MAP.getBasicMap().getById(pl.PARENT);
		
		
		Collection<StarSystem> sector = new HashSet<StarSystem>();
		char secId = 'i';
		
		for (Integer i : ACTIONS.ACT.MAP.SECTORS.getById(secId).getContents())
			sector.add((StarSystem) ACTIONS.ACT.MAP.getBasicMap().getById(i.intValue()));
		
		Sector s = ACTIONS.ACT.MAP.SECTORS.getById(secId);
		
		MASTER.pl("Sector " + s.ID + " contents:");
		for (Integer i : s.getContents())
			MASTER.pr(i.intValue() + " ");
		
		try
		{
			ACTIONS.ACT.smartTour(fl, sector, ss);
		}
		catch (Exception e)
		{
			MASTER.pl("Test failed!");
		}
		
	}
	
	private void proofOfConcept()
	{
		
		MASTER.pl("\nThis is a proof-of-concept run. " +
				"\nIts aim is to scout the entire game-world, sector by sector (9 sectors overall)," +
				"\ngoverened by a somewhat optimezed algorithm \n(a search tree for each universe-sector for a dynamically-set 'K' greedy solutions).\n");
		
		
		Sectors sectors = ACTIONS.ACT.MAP.SECTORS;
		
		//get the default scout fleet:
		Collection<Fleet> myFleet = new HashSet<Fleet>();
		
		for (Sector sector : sectors.SECTORS)
			myFleet.addAll(ACTIONS.ACT.MAP.getAllMyFleet(sector));
		
		
		MASTER.pl("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		MASTER.pl("BOGUS STORYLINE:");
		MASTER.pl("A committee of scientists and used car salesmen have decided to map out the whole known universe...");
		MASTER.pl("It took a while, but a highly advanced space-craft was put into orbit by the end of the year. " +
				"It was governed by some very sophisticated software...");
		MASTER.pl("And so, it set out on its impossible mission, never to be seen again.");
		MASTER.pl("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		
		//there is only one at the start of the game:
		Fleet defaultScoutFleet = (Fleet)myFleet.toArray()[0];
		
		//print the location of that fleet:
		StarSystem home = (StarSystem)ACTIONS.ACT.MAP.getBasicMap().getById(defaultScoutFleet.PARENT);
		MASTER.pl("The fleet set out from: " + home.NAME);
		
		
		MASTER.pl("Doing a sector-by-sector planning of the route:");
		
		long t1 = System.currentTimeMillis(); //the starting time
		
		//scout the whole universe:
		char[] idsInPrettyOrder = {'a', 'b', 'c', 'f', 'e', 'd', 'g', 'h', 'i'};
		try
		{
			for (int i = 0; i < idsInPrettyOrder.length; i++)
			{
				char currentSector = idsInPrettyOrder[i];
				MASTER.pl("Planning route for sector: " + currentSector);
				ACTIONS.scoutSectorAndStayThere(defaultScoutFleet, sectors.getById(currentSector));
			}
		}
		catch (Exception e)
		{
			MASTER.exit("Unexpected failure. AI-Bot actions failed.", Master.ABNORMAL_EXIT, e);
		}
			
		long tookTime1 = System.currentTimeMillis() - t1;
		MASTER.pl("The overall run took " + tookTime1 + " milliseconds");
		
		
		/*
		long t2 = System.currentTimeMillis();
		MASTER.pl("Planning for the whole universe at once:");
		try
		{
			ACTIONS.ACT.smartTour(defaultScoutFleet, ACTIONS.ACT.MAP.getBasicMap().STAR_SYSTEMS, home); 
		}
		catch (Exception e)
		{
			MASTER.exit("Unexpected failure. AI-Bot actions failed (full universe planning).", Master.ABNORMAL_EXIT, e);
		}
		long tookTime2 = System.currentTimeMillis() - t2;
		MASTER.pl("The overall run took " + tookTime2 + " milliseconds");
		*/
		
		MASTER.exit("SUCCESSFUL RUN: The brave scout ship has set out to map the entire universe, and was never seen again...", Master.NORMAL_EXIT, null);
	}
}
