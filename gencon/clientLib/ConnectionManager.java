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
 * Very simple connection manager, which implements an underlying {@link PipelinedConnection}.
 * It's functionality, is to simply keep track of all pipelines, and close them in the end,
 * in the event they weren't closed manually.
 * 
 * @author Victor Ivri
 *
 */
public class ConnectionManager<V extends Visitor>
{
	private final PipelinedConnection<V> pConn;
	private List<SequentialConnection<V>> pipelines;
	private final Client client;
	
	public ConnectionManager(Connection<V> connection, Client client)
	{
		pConn = new PipelinedConnection<V>(connection);
		pipelines = Collections.synchronizedList(new Vector<SequentialConnection<V>>());
		this.client = client;
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
			Utils.PrintTraceIfDebug(e, client);
		}
	}
	
	
	
}
