package gencon.clientLib;

import net.thousandparsec.netlib.Frame;
import net.thousandparsec.netlib.TPException;
import net.thousandparsec.netlib.tp03.Fail;
import net.thousandparsec.netlib.tp03.TP03Visitor;
import net.thousandparsec.netlib.tp03.TimeRemaining;

/**
 * A {@link TP04Visitor} tailored for Genetic Conquest.
 * 
 * @author Victor Ivri
 *
 */
public class GCTP03Visitor extends TP03Visitor
{
	private final Client CLIENT;
	
	//to determine turn time:
	private int turn_duration = 15; //default value. Shouldn't go below it.
	
	public GCTP03Visitor(Client client)
	{
		super();
		CLIENT = client;
	}
	
	@Override
	public void unhandledFrame(Frame<?> frame) throws TPException
	{
		throw new TPException(String.format("Unexpected frame: type %d (%s)", frame.getFrameType(), frame.toString()));
	}

    @Override
    public void frame(TimeRemaining frame)
    {
    	if (frame.getTime() != 0)
    	{
    		if (frame.getTime() > turn_duration) //the turn_duration should always be the biggest time frame.
    			turn_duration = frame.getTime();
    		
    		if (frame.getTime() == turn_duration)
    			CLIENT.pushTurnStartFlag();
    	}
    	//else, do nothing.
    }
    
    @Override
    public void frame(Fail frame)
    {
    	System.err.println("Unexpected failure. Reason: " + frame.getResult());
    }

}
