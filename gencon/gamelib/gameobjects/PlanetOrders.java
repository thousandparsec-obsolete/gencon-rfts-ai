package gencon.gamelib.gameobjects;

import java.util.Vector;

public class PlanetOrders
{
	public final Vector<POrder> ORDERS;
	
	
	public PlanetOrders(Vector<POrder> orders)
	{
		ORDERS = orders;
	}
	
	public enum POrderType
	{
		INDUSTRY, SOC_ENV, PLAN_ENV, POP_MAINT, COLONIZE; //ETC!!!
	}
	
	public class POrder extends GenericOrder
	{
		public final POrderType TYPE;
		
		//HOW DO I REPRESENT QUANTITY???
		
		public POrder(POrderType type, int order_type_id, int object_id, int place_in_queue) 
		{
			super(order_type_id, object_id, place_in_queue);
			TYPE = type;
		}
	}
}
