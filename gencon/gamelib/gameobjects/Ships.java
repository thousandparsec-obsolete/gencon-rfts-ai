package gencon.gamelib.gameobjects;

/**
 * A class representing ships in a {@link Fleet}.
 * 
 * @author Victor Ivri
 */
public class Ships 
{
	public final int TRANSPORTS;
	public final int SCOUTS;
	public final int MK1S;
	public final int MK2S;
	public final int MK3S;
	public final int MK4S;
	
	public Ships(int transports, int scouts, int mk1, int mk2, int mk3, int mk4)
	{
		TRANSPORTS = transports;
		SCOUTS = scouts;
		MK1S = mk1;
		MK2S = mk2;
		MK3S = mk3;
		MK4S = mk4;
	}

}
