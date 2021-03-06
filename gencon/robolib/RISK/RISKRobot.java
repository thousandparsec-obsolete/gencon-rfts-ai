package gencon.robolib.RISK;

import java.io.IOException;
import java.util.Collection;

import gencon.Master;
import gencon.clientLib.RISK.ClientMethodsRISK;
import gencon.evolutionlib.Genotype;
import gencon.gamelib.Players;
import gencon.gamelib.Players.Game_Player;
import gencon.gamelib.RISK.FullGameStatusRISK;
import gencon.gamelib.RISK.UniverseMap;
import gencon.gamelib.RISK.gameobjects.Star;
import gencon.robolib.Robot;

public class RISKRobot extends Robot
{
	public final Master MASTER;
	public final short DIFFICULTY;
	public final ClientMethodsRISK CLIENT_RISK;
	public final FullGameStatusRISK FGS;
	public final ActionController CONTROLLER;
	public final AdvancedMap MAP;

	public RISKRobot(Master master, Genotype genome) 
	{
		super(genome);
		MASTER = master;
		DIFFICULTY = MASTER.getDifficulty();
		CLIENT_RISK = (ClientMethodsRISK) MASTER.CLIENT.getClientMethods();
		FGS = (FullGameStatusRISK) MASTER.getStatus();
		MAP = new AdvancedMap();
		CONTROLLER = new ActionController(new ActionMethods(MAP, CLIENT_RISK, MASTER.out), MASTER.out);
	}

	@Override
	public void startTurn(int time_remaining, int turn_num) throws IOException
	{
		MASTER.out.pr("Initializing bot with new data..... ");
		super.startTurn(time_remaining, turn_num);
		UniverseMap mapForSimulations = FGS.getCurrentStatus().left;
		
		CONTROLLER.incrementTurn(mapForSimulations, FGS.getCurrentStatus().right.getMe().NUM);
		
		//generating output:
		Collection<Game_Player> players = FGS.getCurrentStatus().right.PLAYERS;
		Game_Player neutral = new Players.Game_Player(-1, "Neutral");
		players.add(neutral);
		MAP.printData(MASTER.out, players);
		
		MASTER.out.pl("Engaging in action.");
		CONTROLLER.performActions(getCurrentTraits());
		MASTER.out.pl("Finished performing actions for this turn.");
	}
	
	
	public void pl(String st)
	{
		MASTER.out.pl(st);
	}
	
	public void pr(String st)
	{
		MASTER.out.pr(st);
	}
	
	public void test()
	{
		testColonize();
		testMove();
		testReinforce();
	}
	
	private void testColonize()
	{
		Star neutral = null;
		for (Star s :FGS.getCurrentStatus().left.getStars())
			if (s.getOwner() == -1)
			{
				neutral = s;
				break;
			}
		
		try
		{
			boolean b = CLIENT_RISK.orderColonize(neutral, 15, false);
			System.out.println("Tried to colonize: " + neutral.NAME + " with 15 troops. Result: " + b);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private void testMove()
	{
		Star star = null;
		for (Star s : FGS.getCurrentStatus().left.getStars())
			if (s.getOwner() == FGS.getCurrentStatus().right.getMe().NUM)
			{
				star = s;
				break;
			}
			
			
		Star someNeighbor = FGS.getCurrentStatus().left.getStarWithId(star.getAdjacencies().iterator().next());
		
		try
		{
			boolean c = CLIENT_RISK.orderMove(star, someNeighbor, 1, false);
			System.out.println("Tried to move to: " + someNeighbor.NAME + " from: " + star.NAME + " with 1 troops. Result: " + c);
	
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}

	}
	
	private void testReinforce()
	{
		Star star = null;
		for (Star s : FGS.getCurrentStatus().left.getStars())
			if (s.getOwner() == FGS.getCurrentStatus().right.getMe().NUM)
			{
				star = s;
				break;
			}
		
		try
		{
			boolean c = CLIENT_RISK.orderReinforce(star, 19, false);
			System.out.println("Tried to reinforce: " + star.NAME + " with 19 troops. Result: " + c);
	
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}
	}

	


}
