package gencon.robolib.RFTS;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import gencon.clientLib.Client;
import gencon.evolutionlib.Genotype;
import gencon.gamelib.RFTS.FullGameStatusRFTS;
import gencon.gamelib.RFTS.gameobjects.Fleet;
import gencon.gamelib.RFTS.gameobjects.Planet;
import gencon.gamelib.RFTS.gameobjects.StarSystem;
import gencon.robolib.Robot;
import gencon.robolib.RFTS.AdvancedMap.Sectors;
import gencon.robolib.RFTS.AdvancedMap.Sectors.Sector;


public class RFTSRobot extends Robot
{
	public enum TURN_TYPE
	{
		PRODUCTION, MOVEMENT, BUILD_FLEET;
	}
	public final short DIFFICULTY;
	public final Client CLIENT;
	public final FullGameStatusRFTS FGS;
	private final HigherLevelActions ACTIONS;
	
	public static final byte WORK_TIME = 10; //The time, in seconds, required to complete a turn. 
	//if remaining time in some turn is less than that, robot will not execute until next turn.
	//ONLY AN ESTIMATE NOW!!! NEED TO CALCULATE ACTUAL TIMES! (MAY DEPEND ON PING).
	
	private TURN_TYPE turnType;

	
	public RFTSRobot(Genotype genome, Client client, FullGameStatusRFTS fgs, short difficulty, int turn_num)
	{
		super(genome);
		CLIENT = client;
		FGS = fgs;
		DIFFICULTY = difficulty;
		ACTIONS = new HigherLevelActions(new ActionMethods(CLIENT, fgs));
		determineTurn(turn_num);
	}
	
	/*
	 * Gets the type of RFTS turn from the turn number.
	 */
	private void determineTurn(int turn)
	{
		byte convert = (byte)(turn % 3);
		
		switch (convert)
		{
			case (0): turnType = TURN_TYPE.PRODUCTION;
			case (1): turnType = TURN_TYPE.MOVEMENT;
			case (2): turnType = TURN_TYPE.BUILD_FLEET;
		}
	}
	
	@Override
	public void startTurn(int time_remaining, int turn)
	{
		super.startTurn(time_remaining, turn);
		incrementTurn();
		
		/*
		 * 1) evaluate situation.
		 * 2) act on it!
		 */
		
		//test();
		try
		{
			proofOfConcept();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void incrementTurn()
	{
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
			test_pr("Sector " + sc.ID + " contains: ");
			for (Integer i : sc.getContents())
				test_pr(" " + ACTIONS.ACT.MAP.getBasicMap().getById(i.intValue()).NAME);
			test_pl("");
			
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
		
		test_pl("Sector " + s.ID + " contents:");
		for (Integer i : s.getContents())
			test_pr(i.intValue() + " ");
		
		try
		{
			ACTIONS.ACT.smartTour(fl, sector, ss);
		}
		catch (Exception e)
		{
			test_pl("Test failed!");
		}
		
	}
	
	private void proofOfConcept() throws Exception
	{
		
		test_pl("\nThis is a proof-of-concept run. " +
				"\nIts aim is to scout the entire game-world, sector by sector (9 sectors overall)," +
				"\ngoverened by a somewhat optimezed algorithm \n(a search tree for each universe-sector for a dynamically-set 'K' greedy solutions).\n");
		
		
		Sectors sectors = ACTIONS.ACT.MAP.SECTORS;
		
		//get the default scout fleet:
		Collection<Fleet> myFleet = new HashSet<Fleet>();
		
		for (Sector sector : sectors.SECTORS)
			myFleet.addAll(ACTIONS.ACT.MAP.getAllMyFleet(sector));
		
		
		test_pl("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		test_pl("BOGUS STORYLINE:");
		test_pl("A committee of scientists and used car salesmen have decided to map out the whole known universe...");
		test_pl("It took a while, but a highly advanced space-craft was put into orbit by the end of the year. " +
				"It was governed by some very sophisticated software...");
		test_pl("And so, it set out on its impossible mission, never to be seen again.");
		test_pl("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		
		//there is only one at the start of the game:
		Fleet defaultScoutFleet = (Fleet)myFleet.toArray()[0];
		
		//print the location of that fleet:
		StarSystem home = (StarSystem)ACTIONS.ACT.MAP.getBasicMap().getById(defaultScoutFleet.PARENT);
		test_pl("The fleet set out from: " + home.NAME);
		
		
		test_pl("Doing a sector-by-sector planning of the route:");
		
		long t1 = System.currentTimeMillis(); //the starting time
		
		//scout the whole universe:
		char[] idsInPrettyOrder = {'a', 'b', 'c', 'f', 'e', 'd', 'g', 'h', 'i'};
		try
		{
			for (int i = 0; i < idsInPrettyOrder.length; i++)
			{
				char currentSector = idsInPrettyOrder[i];
				test_pl("Planning route for sector: " + currentSector);
				ACTIONS.scoutSectorAndStayThere(defaultScoutFleet, sectors.getById(currentSector));
			}
		}
		catch (Exception e)
		{
			throw new Exception("Unexpected failure. AI-Bot actions failed.");
		}
			
		long tookTime1 = System.currentTimeMillis() - t1;
		test_pl("The overall run took " + tookTime1 + " milliseconds");
		
		
		/*
		long t2 = System.currentTimeMillis();
		test_pl("Planning for the whole universe at once:");
		try
		{
			ACTIONS.ACT.smartTour(defaultScoutFleet, ACTIONS.ACT.MAP.getBasicMap().STAR_SYSTEMS, home); 
		}
		catch (Exception e)
		{
			MASTER.exit("Unexpected failure. AI-Bot actions failed (full universe planning).", Master.ABNORMAL_EXIT, e);
		}
		long tookTime2 = System.currentTimeMillis() - t2;
		test_pl("The overall run took " + tookTime2 + " milliseconds");
		*/
		
		throw new Exception("SUCCESSFUL RUN: The brave scout ship has set out to map the entire universe, and was never seen again...");
	}
	
	/*
	 * Print line for tests.
	 */
	private void test_pl(String st)
	{
		System.out.println(st);
	}
	
	/*
	 * Print for tests.
	 */
	private void test_pr(String st)
	{
		System.out.print(st);
	}
}
