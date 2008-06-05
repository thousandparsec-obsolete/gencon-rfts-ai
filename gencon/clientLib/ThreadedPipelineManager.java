package gencon.clientLib;

//import gencon.*;
import net.thousandparsec.netlib.*;

import java.io.IOException;
import java.util.*;

/**
 * 		~~~STILL IN PROTOTYPE FORM~~~ UNSURE OF BEST ROUTE FOR IMPLEMENTATION.
 * 
 * 
 * @author Victor Ivri
 *
 */
public class ThreadedPipelineManager<V extends Visitor> implements Runnable
{
	private final PipelinedConnection<V> pConn;
	private final Vector<SimpleSequentialConnection<V>> pipelines;
	
	public ThreadedPipelineManager(Connection<V> connection)
	{
		pConn = new PipelinedConnection<V>(connection);
		pipelines = new Vector<SimpleSequentialConnection<V>>();
	}
	
	public void run()
	{
		while (true)
		{
			try
			{
				Thread.sleep(1);
			}
			catch (InterruptedException e)
			{
				close();
			}
		}
	}
	
	/**
	 * 
	 * @return SimpleSequentialConnection<V>, which is running in a separate thread.
	 */
	public SimpleSequentialConnection<V> getPipeline()
	{
		SimpleSequentialConnection<V> pipeline = (SimpleSequentialConnection<V>)pConn.createPipeline();
		
		pipelines.add(pipeline);
		return pipeline;
		
	}
	
	private void close()
	{
		for (SimpleSequentialConnection<V> conn : pipelines)
		{
			if (conn != null)
			{
				try
				{
					conn.close();
				}
				catch (IOException e)
				{
					System.out.println("failed to close pipeline: " + conn.toString());
				}
			}
		}
		
		try
		{
			pConn.close();
		}
		catch (Exception e)
		{
			System.out.println("Failed to close pipelined connection.");
		}
	}
	
	
	
}
