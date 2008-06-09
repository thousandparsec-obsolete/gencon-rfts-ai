package gencon.clientLib;

import net.thousandparsec.netlib.*;
import net.thousandparsec.netlib.tp03.*;

public class GCTP03Visitor extends TP03Visitor
{
	
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
            System.err.println("Failed to receive expected frame.");
    }
}
