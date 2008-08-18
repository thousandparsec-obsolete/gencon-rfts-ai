package gencon.clientLib;

import net.thousandparsec.netlib.*;
import net.thousandparsec.netlib.tp03.TP03Visitor;

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
public class ConnectionManager<V extends TP03Visitor>
{
	private final PipelinedConnection<V> pConn;
	private List<SequentialConnection<V>> pipelines;
	
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
	
	/**
	 * Closes and removes all active pipelines to free up memory.
	 */
	public synchronized void purgePipelines()
	{
		for (int i = 0; i < pipelines.size(); i++)
			if (pipelines.get(i) != null)
			{
				try
				{
					pipelines.get(i).close();
				}
				catch (Exception e)
				{
					System.out.println("Failed to close pipeline.");
				}
				pipelines.remove(i);
			}
	}
	
	
	public synchronized void close() throws IOException, ExecutionException
	{
		for (SequentialConnection<V> conn : pipelines)
		{
			try
			{
				conn.close();
			}
			catch (Exception ignore){}
		}
		
		pConn.close();
	}
	
	
	
}
