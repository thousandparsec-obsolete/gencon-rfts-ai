package gencon.robolib.RISK;

import gencon.clientLib.Client;
import gencon.clientLib.RISK.ClientMethodsRISK;
import gencon.evolutionlib.Genotype;
import gencon.gamelib.RISK.FullGameStatusRISK;
import gencon.gamelib.RISK.UniverseMap;
import gencon.gamelib.RISK.gameobjects.Star;
import gencon.robolib.Robot;

public class RISKRobot extends Robot
{
	
	public final short DIFFICULTY;
	public final Client CLIENT;
	public final ClientMethodsRISK CLIENT_RISK;
	public final FullGameStatusRISK FGS;
	public final AdvancedMap ADVANCED_MAP;

	public RISKRobot(Genotype genome, Client client, FullGameStatusRISK fgs, short difficulty) 
	{
		super(genome);
		DIFFICULTY = difficulty;
		CLIENT = client;
		CLIENT_RISK = (ClientMethodsRISK) CLIENT.getClientMethods();
		FGS = fgs;
		ADVANCED_MAP = new AdvancedMap();
	}

	@Override
	public void startTurn(int time_remaining) 
	{
		super.startTurn(time_remaining);
		
		UniverseMap simulationMap = FGS.getCurrentStatus().left;
		int myPlrNum = FGS.getCurrentStatus().right.getMe().NUM;
		
		ADVANCED_MAP.updateMap(simulationMap, myPlrNum);
		
		
		
		
		
		//test();
		
		
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
