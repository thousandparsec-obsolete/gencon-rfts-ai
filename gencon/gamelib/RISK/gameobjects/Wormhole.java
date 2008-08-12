package gencon.gamelib.RISK.gameobjects;

public class Wormhole extends RiskGameObject
{
	public final String END_A;
	public final String END_B;
	
	public Wormhole(String end_a, String end_b) 
	{
		super("", (short) -1); //doesn't matter!
		END_A = end_a;
		END_B = end_b;
	}
}
