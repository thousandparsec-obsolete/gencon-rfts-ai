package gencon.clientLib;

import net.thousandparsec.netlib.Frame;
import net.thousandparsec.netlib.TPException;
import net.thousandparsec.netlib.tp04.*;

public class GCTP04Visitor extends TP04Visitor
{
	
	@Override
	public void unhandledFrame(Frame<?> frame) throws TPException
	{
		throw new TPException(String.format("Unexpected frame: type %d (%s)", frame.getFrameType(), frame.toString()));
	}

    @Override
    public void frame(TimeRemaining frame)
    {
            System.err.println("new turn. Time remaining: " + frame.getTime());
    }

    @Override
    public void frame(Fail frame)
    {
            System.err.println("Request failed. Frame Seq. num.: " + frame.getSequenceNumber() + "| Reason: " + frame.getResult());
    }

}
