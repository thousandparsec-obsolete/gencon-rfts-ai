package gencon.robolib.RFTS;

import gencon.Master;
import gencon.evolutionlib.Genotype;
import gencon.robolib.RoboBrain;


public class RFTSBrain extends RoboBrain
{
	public final Master MASTER;
	
	public final short DIFFICULTY;
	
	private final HigherLevelActions ACTIONS;
	
	public RFTSBrain(Genotype genome, Master master, short difficulty)
	{
		super(genome);
		MASTER = master;
		DIFFICULTY = difficulty;
		
		ACTIONS = new HigherLevelActions(new ActionMethods(MASTER.CLIENT, MASTER.GAME_STATUS));
	}

}
