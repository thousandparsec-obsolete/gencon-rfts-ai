package gencon.clientLib;

//import gencon.*;
import net.thousandparsec.netlib.*;
import net.thousandparsec.netlib.tp03.TP03Visitor;
import net.thousandparsec.util.*;
import gencon.utils.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * 		~~~STILL IN PROTOTYPE FORM~~~ UNSURE OF BEST ROUTE FOR IMPLEMENTATION.
 * 
 * 
 * @author Victor Ivri
 *
 */
public class ConnectionManager<V extends Visitor>
{
	private final PipelinedConnection<V> pConn;
	private final List<SequentialConnection<V>> pipelines;
	
	public ConnectionManager(Connection<V> connection)
	{
		pConn = new PipelinedConnection<V>(connection);
		pipelines = Collections.synchronizedList(new Vector<SequentialConnection<V>>());
	}
	
	/**
	 * 
	 * @return SequentialConnection<V>, which is running in a separate thread.
	 */
	public synchronized SequentialConnection<V> createPipeline()
	{
		SequentialConnection<V> pipeline = pConn.createPipeline();
		
		pipelines.add(pipeline);
		return pipeline;
		
	}
	
	public synchronized void close()
	{
		for (SequentialConnection<V> conn : pipelines)
		{
			try
			{
				conn.close();
			}
			catch (Exception ignore){}
		}
		
		try
		{
			pConn.close();
		}
		catch (Exception e)
		{
			System.err.println("Failed to close pipelined connection.");
			Utils.PrintTraceIfDebug(e, true);
		}
	}
	
	
	
}