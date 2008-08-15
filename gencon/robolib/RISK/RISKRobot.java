package gencon.robolib.RISK;

import gencon.clientLib.Client;
import gencon.clientLib.RISK.ClientMethodsRISK;
import gencon.evolutionlib.Genotype;
import gencon.gamelib.RISK.FullGameStatusRISK;
import gencon.gamelib.RISK.gameobjects.Star;
import gencon.robolib.Robot;

public class RISKRobot extends Robot
{
	
	public final short DIFFICULTY;
	public final Client CLIENT;
	public final ClientMethodsRISK CLIENT_RISK;
	public final FullGameStatusRISK FGS;

	public RISKRobot(Genotype genome, Client client, FullGameStatusRISK fgs, short difficulty) 
	{
		super(genome);
		DIFFICULTY = difficulty;
		CLIENT = client;
		CLIENT_RISK = (ClientMethodsRISK) CLIENT.getClientMethods();
		FGS = fgs;
	}

	@Override
	public void startTurn(int time_remaining) 
	{
		super.startTurn(time_remaining);
		test();
	}
	
	
	public void test()
	{
		
		//testing colonize:
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

		//testing move:
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
		/*
		//testing reinforce:
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
		
		*/
	}

	


}
