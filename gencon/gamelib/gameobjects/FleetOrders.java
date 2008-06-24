package gencon.gamelib.gameobjects;

import java.util.Vector;

public class FleetOrders
{
	public final Vector<FOrder> ORDERS;
	
	public FleetOrders(Vector<FOrder> orders)
	{
		ORDERS = orders;
	}
	
	public enum FOrderType
	{
		MOVE, MERGE, COLONIZE; //OTHERS???
	}
	
	public class FOrder extends GenericOrder
	{
		public final FOrderType TYPE;

		//HOW DO I REPRESENT QUANTITY, AND TARGET???
		
		public FOrder(FOrderType type, int order_type_id, int object_id, int place_in_queue) 
		{
			super(order_type_id, object_id, place_in_queue);
			TYPE = type;
		}
	}
}