package gencon.clientLib;

import net.thousandparsec.netlib.Frame;
import net.thousandparsec.netlib.TPException;
import net.thousandparsec.netlib.tp04.*;
import net.thousandparsec.netlib.tp04.TimeRemaining.Reason;

public class GCTP04Visitor extends TP04Visitor
{
	private final Client CLIENT;
	
	public GCTP04Visitor(Client client)
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
    	if (frame.getReason() ==  Reason.TimerStarted)
    		CLIENT.pushTurnStartFlag();
    	
    	//else, do nothing.
    }

    @Override
    public void frame(Fail frame)
    {
    	System.err.println("Request failed. Frame Seq. num.: " + frame.getSequenceNumber() + "| Reason: " + frame.getResult());
    }

}
