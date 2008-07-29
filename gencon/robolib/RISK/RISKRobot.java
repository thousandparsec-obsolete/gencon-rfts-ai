package gencon.robolib.RISK;

import gencon.clientLib.Client;
import gencon.evolutionlib.Genotype;
import gencon.gamelib.RISK.FullGameStatusRISK;
import gencon.robolib.Robot;

public class RISKRobot extends Robot
{
	
	public final short DIFFICULTY;
	public final Client CLIENT;
	public final FullGameStatusRISK FGS;

	public RISKRobot(Genotype genome, Client client, FullGameStatusRISK fgs, short difficulty) 
	{
		super(genome);
		DIFFICULTY = difficulty;
		CLIENT = client;
		FGS = fgs;
	}

	@Override
	public void startTurn(int time_remaining) 
	{
		super.startTurn(time_remaining);
	}

	


}
