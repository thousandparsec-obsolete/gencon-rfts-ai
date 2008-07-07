package gencon.gamelib.gameobjects;

import java.util.List;
import java.util.Vector;

public class FleetOrders
{
	public final static int MOVE_ORDER = 2;
	public final static int SPLIT_ORDER = 3;
	public final static int MERGE_ORDER = 4;
	public final static int RENAME_ORDER = 5;
	public final static int COLONISE_ORDER = 6;
	public final static int BOMBARD_ORDER = 7;
	
	
	public final List<FOrder> ORDERS;
	
	public FleetOrders(List<FOrder> orders)
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