package gencon.clientLib;

import net.thousandparsec.netlib.Frame;
import net.thousandparsec.netlib.TPException;
import net.thousandparsec.netlib.tp03.TP03Visitor;

public class GCTP03Visitor extends TP03Visitor
{
	
	
	
	@Override
	public void unhandledFrame(Frame<?> frame) throws TPException
	{
		//if (errorOnUnhandled)
			//throw new TPException(String.format("Unexpected frame: type %d (%s)", frame.getFrameType(), frame.toString()));
		
		handleAsynchFrame(frame);
	}
	
	
	private void handleAsynchFrame(Frame<?> frame)
	{
		if (frame.getClass().getSimpleName().equals("TimeRemaining")) //new turn
		{
			System.out.println("new turn");
		}
		
		else if (frame.getClass().getSimpleName().equals("Fail")) //new turn
		{
			System.out.println("Failed to receive expected frame.");
		}
	}
}
