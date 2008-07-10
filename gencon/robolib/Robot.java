package gencon.robolib;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import net.thousandparsec.netlib.tp03.Object;

import gencon.Master;
import gencon.clientLib.Client;
import gencon.gamelib.FullGameStatus;
import gencon.gamelib.gameobjects.Fleet;
import gencon.gamelib.gameobjects.Planet;
import gencon.gamelib.gameobjects.StarSystem;
import gencon.robolib.AdvancedMap.Sectors;
import gencon.robolib.AdvancedMap.Sectors.Sector;

/**
 * The AI robot, which will play the game.
 * 
 * @author Victor Ivri
 * 
 */
public class Robot 
{
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

	
	public Robot(Master master) throws Exception
	{
		MASTER = master;
		CHARACTER = new Phenotype(new Genotype(MASTER.CLIENT.getGenomeFileClasspath()));
		DIFFICULTY = MASTER.CLIENT.getDifficulty();
		ACTIONS = new HigherLevelActions(new ActionMethods(MASTER.CLIENT, MASTER.GAME_STATUS));
	}
	
	public void startTurn(int time_remaining)
	{
		turn_num ++;
		CHARACTER.updatePhenotype(turn_num);
		
		/*
		 * 1) evaluate situation.
		 * 2) act on it!
		 */
		
		//test();
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
}
